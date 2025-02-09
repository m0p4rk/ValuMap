package com.uga.hacksX.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.uga.hacksX.exception.ExternalApiException;
import com.uga.hacksX.model.worldbank.ExchangeRates;
import com.uga.hacksX.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final WebClient exchangeRateWebClient;
    
    @Value("${api.exchange.key}")
    private String apiKey;

    @Override
    public Mono<ExchangeRates> getExchangeRates(String baseCurrency) {
        log.debug("Fetching exchange rates for base currency: {}", baseCurrency);
        
        return exchangeRateWebClient.get()
                .uri("/latest?base={base}&symbols=KRW,JPY,USD,EUR,THB&apikey={key}", 
                     baseCurrency, apiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    JsonNode rates = response.get("rates");
                    Map<String, Double> rateMap = new HashMap<>();
                    rates.fields().forEachRemaining(entry -> 
                        rateMap.put(entry.getKey(), entry.getValue().asDouble()));
                    
                    String date = response.get("date").asText();
                    log.debug("Retrieved exchange rates for {}: {}", baseCurrency, rateMap);
                    
                    return new ExchangeRates(baseCurrency, rateMap, date);
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof ExternalApiException))
                .onErrorResume(e -> {
                    log.error("Error fetching exchange rates: {}. Using fallback values.", e.getMessage());
                    return getFallbackRates(baseCurrency);
                });
    }

    private Mono<ExchangeRates> getFallbackRates(String baseCurrency) {
        Map<String, Double> fallbackRates = new HashMap<>();
        String today = java.time.LocalDate.now().toString();

        // Fallback rates based on USD
        switch (baseCurrency) {
            case "KRW" -> {
                fallbackRates.put("KRW", 1.0);
                fallbackRates.put("USD", 0.00077); // 1300 KRW = 1 USD
                fallbackRates.put("EUR", 0.00071); // 1400 KRW = 1 EUR
                fallbackRates.put("JPY", 0.1);     // 10 KRW = 1 JPY
                fallbackRates.put("THB", 0.029);   // 35 KRW = 1 THB
            }
            case "USD" -> {
                fallbackRates.put("KRW", 1300.0);
                fallbackRates.put("USD", 1.0);
                fallbackRates.put("EUR", 0.92);
                fallbackRates.put("JPY", 130.0);
                fallbackRates.put("THB", 35.0);
            }
            default -> {
                // Convert through USD rates if other currency is base
                Map<String, Double> usdRates = new HashMap<>();
                usdRates.put("KRW", 1300.0);
                usdRates.put("USD", 1.0);
                usdRates.put("EUR", 0.92);
                usdRates.put("JPY", 130.0);
                usdRates.put("THB", 35.0);

                double baseToUsd = usdRates.get(baseCurrency);
                usdRates.forEach((currency, rate) -> 
                    fallbackRates.put(currency, rate / baseToUsd));
            }
        }

        log.warn("Using fallback exchange rates for {}", baseCurrency);
        return Mono.just(new ExchangeRates(baseCurrency, fallbackRates, today));
    }

    @Override
    public Mono<Double> convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return Mono.just(amount);
        }

        return getExchangeRates(fromCurrency)
                .map(rates -> {
                    double rate = rates.getRate(toCurrency);
                    double converted = amount * rate;
                    log.debug("Converted {} {} to {} {}", 
                            amount, fromCurrency, converted, toCurrency);
                    return converted;
                });
    }

    @Override
    public Mono<Double> getExchangeRate(String baseCurrency, String targetCurrency) {
        log.debug("Fetching exchange rate from {} to {}", baseCurrency, targetCurrency);
        return exchangeRateWebClient.get()
                .uri("/latest/{base}", baseCurrency)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    if (response.has("rates") && response.get("rates").has(targetCurrency)) {
                        return response.get("rates").get(targetCurrency).asDouble();
                    }
                    throw new ExternalApiException("Exchange rate not found for " + targetCurrency);
                })
                .doOnError(e -> log.error("Error fetching exchange rate: {}", e.getMessage()))
                .onErrorResume(e -> Mono.just(1.0)); // 에러 시 기본값 반환
    }
}
