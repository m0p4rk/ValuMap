package com.uga.hacksX.service;

import com.uga.hacksX.model.worldbank.WorldBankIndicator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorldBankService {
    Mono<WorldBankIndicator> getCountryInfo(String countryCode);
    Mono<WorldBankIndicator> getCostOfLivingIndex(String countryCode);
    Mono<WorldBankIndicator> getGdpPerCapita(String countryCode);
    Mono<WorldBankIndicator> getExchangeRate(String countryCode);
    Flux<WorldBankIndicator> getAllCountries();
} 