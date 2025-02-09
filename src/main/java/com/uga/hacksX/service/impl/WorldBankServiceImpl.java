package com.uga.hacksX.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uga.hacksX.exception.ExternalApiException;
import com.uga.hacksX.model.worldbank.WorldBankIndicator;
import com.uga.hacksX.service.ExchangeRateService;
import com.uga.hacksX.service.WorldBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldBankServiceImpl implements WorldBankService {
    private final WebClient worldBankWebClient;
    private final ExchangeRateService exchangeRateService;
    
    @Value("${api.worldbank.base-url}")
    private String baseUrl;

    private static final Map<String, String> COUNTRY_CURRENCIES = Map.of(
            "KR", "KRW", 
            "JP", "JPY", 
            "US", "USD",
            "FR", "EUR", 
            "TH", "THB"
    );

    private static final String GDP_INDICATOR = "NY.GDP.PCAP.PP.CD";
    private static final String CPI_INDICATOR = "FP.CPI.TOTL";
    private static final String EXCHANGE_INDICATOR = "PA.NUS.FCRF";

    @Override
    public Mono<WorldBankIndicator> getCountryInfo(String countryCode) {
        log.debug("Fetching country info for: {}", countryCode);
        return worldBankWebClient.get()
                .uri("/v2/country/{country}/indicator/NY.GDP.PCAP.PP.CD?format=json&date=2022", countryCode)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response);
                        
                        if (!isValidResponse(root)) {
                            throw new ExternalApiException("Invalid response from World Bank API");
                        }

                        JsonNode data = root.get(1).get(0);
                        return WorldBankIndicator.builder()
                                .countryCode(countryCode)
                                .countryName(getJsonValue(data, "country.value"))
                                .region(getJsonValue(data, "region.value"))
                                .gdpPpp(parseDouble(getJsonValue(data, "value"), 10000.0))
                                .currency(COUNTRY_CURRENCIES.get(countryCode))
                                .build();
                    } catch (Exception e) {
                        log.error("Error parsing response for {}: {}", countryCode, e.getMessage());
                        throw new ExternalApiException("Error parsing World Bank API response", e);
                    }
                })
                .doOnError(e -> log.error("Error fetching country info for {}: {}", countryCode, e.getMessage()));
    }

    @Override
    public Mono<WorldBankIndicator> getGdpPerCapita(String countryCode) {
        log.debug("Fetching GDP data for country: {}", countryCode);
        return getIndicator(countryCode, GDP_INDICATOR, "GDP per capita");
    }

    @Override
    public Mono<WorldBankIndicator> getCostOfLivingIndex(String countryCode) {
        return getIndicator(countryCode, CPI_INDICATOR, "Consumer Price Index");
    }

    @Override
    public Mono<WorldBankIndicator> getExchangeRate(String countryCode) {
        log.debug("Fetching exchange rate for: {}", countryCode);
        String targetCurrency = COUNTRY_CURRENCIES.getOrDefault(countryCode, "USD");
        
        return exchangeRateService.getExchangeRate("KRW", targetCurrency)
                .map(rate -> WorldBankIndicator.builder()
                        .countryCode(countryCode)
                        .indicatorCode(EXCHANGE_INDICATOR)
                        .indicatorName("Exchange Rate")
                        .value(rate)
                        .date(String.valueOf(java.time.Year.now().getValue()))
                        .build())
                .onErrorResume(e -> {
                    log.error("Error fetching exchange rate for {}: {}", countryCode, e.getMessage());
                    return Mono.just(WorldBankIndicator.builder()
                            .countryCode(countryCode)
                            .indicatorCode(EXCHANGE_INDICATOR)
                            .indicatorName("Exchange Rate")
                            .value(getDefaultExchangeRate(countryCode))
                            .date(String.valueOf(java.time.Year.now().getValue()))
                            .build());
                });
    }

    private Mono<WorldBankIndicator> getIndicator(String countryCode, String indicatorCode, String indicatorName) {
        log.debug("Fetching {} for country: {}", indicatorName, countryCode);
        return worldBankWebClient.get()
                .uri("/country/{country}/indicator/{indicator}?format=json&per_page=1", 
                        countryCode, indicatorCode)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .flatMap(response -> parseIndicator(response, countryCode, indicatorCode, indicatorName))
                .next()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException))
                .onErrorResume(throwable -> {
                    log.error("Error fetching {} for {}: {}", 
                            indicatorName, countryCode, throwable.getMessage());
                    return getDefaultIndicator(countryCode, indicatorCode, indicatorName);
                });
    }

    private Mono<WorldBankIndicator> parseIndicator(JsonNode response, 
            String countryCode, String indicatorCode, String indicatorName) {
        try {
            if (!isValidResponse(response)) {
                log.warn("No data found for indicator: {} in country: {}", indicatorName, countryCode);
                return Mono.empty();
            }

            JsonNode data = response.get(1).get(0);
            return Mono.just(WorldBankIndicator.builder()
                    .countryCode(countryCode)
                    .indicatorCode(indicatorCode)
                    .indicatorName(indicatorName)
                    .value(Optional.ofNullable(data.get("value"))
                            .map(JsonNode::asDouble)
                            .orElse(0.0))
                    .date(getJsonValue(data, "date"))
                    .build());
        } catch (Exception e) {
            log.error("Error parsing indicator {} for {}: {}", 
                    indicatorName, countryCode, e.getMessage());
            return Mono.error(new ExternalApiException("Error parsing indicator data", e));
        }
    }

    private boolean isValidResponse(JsonNode response) {
        return response != null && response.isArray() && 
               response.size() > 1 && response.get(1).isArray() && 
               response.get(1).size() > 0;
    }

    private String getJsonValue(JsonNode node, String path) {
        try {
            String[] parts = path.split("\\.");
            JsonNode current = node;
            for (String part : parts) {
                current = current.get(part);
                if (current == null) return "";
            }
            return current.asText();
        } catch (Exception e) {
            log.warn("Error getting JSON value for path {}: {}", path, e.getMessage());
            return "";
        }
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            log.warn("Error parsing double value {}, using default: {}", value, defaultValue);
            return defaultValue;
        }
    }

    private double getDefaultExchangeRate(String countryCode) {
        return switch (countryCode) {
            case "KR" -> 1.0;      // KRW
            case "JP" -> 10.0;     // JPY to KRW
            case "US" -> 1300.0;   // USD to KRW
            case "FR" -> 1400.0;   // EUR to KRW
            case "TH" -> 35.0;     // THB to KRW
            default -> 1.0;
        };
    }

    private Mono<WorldBankIndicator> getDefaultIndicator(
            String countryCode, String indicatorCode, String indicatorName) {
        double defaultValue = switch (indicatorCode) {
            case GDP_INDICATOR -> 30000.0;
            case CPI_INDICATOR -> 100.0;
            case EXCHANGE_INDICATOR -> getDefaultExchangeRate(countryCode);
            default -> 0.0;
        };

        return Mono.just(WorldBankIndicator.builder()
                .countryCode(countryCode)
                .indicatorCode(indicatorCode)
                .indicatorName(indicatorName)
                .value(defaultValue)
                .date("2023")
                .build());
    }

    @Override
    public Flux<WorldBankIndicator> getAllCountries() {
        return Flux.fromIterable(COUNTRY_CURRENCIES.keySet())
            .flatMap(countryCode -> getCountryInfo(countryCode)
                .onErrorResume(e -> {
                    log.error("Error fetching data for country {}: {}", countryCode, e.getMessage());
                    return Mono.empty();
                })
            );
    }
} 