package com.uga.hacksX.service.impl;

import com.uga.hacksX.model.ExchangeRateResponse;
import com.uga.hacksX.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private static final String BASE_URL = "https://api.exchangerate.host/latest";
    private final WebClient webClient;

    public ExchangeRateServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    @Override
    public Mono<ExchangeRateResponse> getExchangeRates(String baseCurrency) {
        log.info("Fetching exchange rates for base currency: {}", baseCurrency);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("base", baseCurrency)
                        .build())
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .doOnSuccess(response -> log.info("Successfully fetched exchange rates"))
                .doOnError(error -> log.error("Error fetching exchange rates: {}", error.getMessage()));
    }
}
