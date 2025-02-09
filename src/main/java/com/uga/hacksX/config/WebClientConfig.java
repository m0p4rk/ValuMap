package com.uga.hacksX.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${api.worldbank.base-url}")
    private String worldBankBaseUrl;

    @Value("${api.exchange.base-url}")
    private String exchangeRateBaseUrl;

    @Bean
    public WebClient worldBankWebClient() {
        return WebClient.builder()
                .baseUrl(worldBankBaseUrl)
                .build();
    }

    @Bean
    public WebClient exchangeRateWebClient() {
        return WebClient.builder()
                .baseUrl(exchangeRateBaseUrl)
                .build();
    }
}