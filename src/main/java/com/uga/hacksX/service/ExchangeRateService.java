package com.uga.hacksX.service;

import com.uga.hacksX.model.worldbank.ExchangeRates;
import reactor.core.publisher.Mono;

public interface ExchangeRateService {
    /**
     * 특정 기준 통화에 대한 환율 정보를 가져옵니다.
     *
     * @param baseCurrency 기준 통화 코드 (예: USD, KRW)
     * @return 환율 정보
     */
    Mono<ExchangeRates> getExchangeRates(String baseCurrency);

    /**
     * 한 통화에서 다른 통화로 금액을 변환합니다.
     *
     * @param amount 변환할 금액
     * @param fromCurrency 원래 통화
     * @param toCurrency 목표 통화
     * @return 변환된 금액
     */
    Mono<Double> convertCurrency(double amount, String fromCurrency, String toCurrency);

    Mono<Double> getExchangeRate(String baseCurrency, String targetCurrency);
}
