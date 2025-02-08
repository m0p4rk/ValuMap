package com.uga.hacksX.service;

import com.uga.hacksX.model.ExchangeRateResponse;
import reactor.core.publisher.Mono;

public interface ExchangeRateService {
    Mono<ExchangeRateResponse> getExchangeRates(String baseCurrency);
}
