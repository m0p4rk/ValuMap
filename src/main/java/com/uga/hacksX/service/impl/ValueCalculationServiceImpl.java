package com.uga.hacksX.service.impl;

import com.uga.hacksX.model.CountryCost;
import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import com.uga.hacksX.service.CostOfLivingService;
import com.uga.hacksX.service.ExchangeRateService;
import com.uga.hacksX.service.ValueCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValueCalculationServiceImpl implements ValueCalculationService {
    private final ExchangeRateService exchangeRateService;
    private final CostOfLivingService costOfLivingService;

    @Override
    public Flux<CountryValue> calculateValues(UserInput userInput) {
        return exchangeRateService.getExchangeRates(userInput.getBaseCurrency())
                .flatMapMany(rates -> costOfLivingService.getAllCosts()
                        .map(countryCost -> calculateCountryValue(countryCost, rates.getRates().get(countryCost.getCurrency()), userInput)));
    }

    private CountryValue calculateCountryValue(CountryCost countryCost, Double exchangeRate, UserInput userInput) {
        if (exchangeRate == null) {
            log.warn("Exchange rate not found for currency: {}", countryCost.getCurrency());
            return null;
        }

        double dailyCost = countryCost.getCosts().get(userInput.getTravelStyle()) * exchangeRate;
        int possibleDays = (int) (userInput.getBudget() / dailyCost);
        double valueScore = calculateValueScore(possibleDays, dailyCost);

        return CountryValue.builder()
                .countryCode(countryCost.getCountryCode())
                .countryName(countryCost.getCountryName())
                .currency(countryCost.getCurrency())
                .exchangeRate(exchangeRate)
                .dailyCost(dailyCost)
                .valueScore(valueScore)
                .possibleDays(possibleDays)
                .build();
    }

    private double calculateValueScore(int possibleDays, double dailyCost) {
        double daysWeight = Math.min(possibleDays / 30.0, 1.0) * 50;
        double costWeight = (1 - Math.min(dailyCost / 200000.0, 1.0)) * 50;
        return daysWeight + costWeight;
    }
}
