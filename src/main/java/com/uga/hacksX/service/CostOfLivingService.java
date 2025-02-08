package com.uga.hacksX.service;

import com.uga.hacksX.model.CountryCost;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CostOfLivingService {
    Flux<CountryCost> getAllCosts();
    Mono<CountryCost> getCostByCountryCode(String countryCode);
}
