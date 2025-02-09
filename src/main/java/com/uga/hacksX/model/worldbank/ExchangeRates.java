package com.uga.hacksX.model.worldbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRates {
    private String baseCurrency;
    private Map<String, Double> rates;
    private String date;

    public double getRate(String currency) {
        return rates != null ? rates.getOrDefault(currency, 1.0) : 1.0;
    }

    public boolean hasRate(String currency) {
        return rates != null && rates.containsKey(currency);
    }

    public String getFormattedRate(String currency) {
        double rate = getRate(currency);
        return String.format("1 %s = %.2f %s", baseCurrency, rate, currency);
    }

    @Override
    public String toString() {
        return String.format("ExchangeRates(base=%s, date=%s, count=%d)", 
            baseCurrency, 
            date,
            rates != null ? rates.size() : 0);
    }
} 