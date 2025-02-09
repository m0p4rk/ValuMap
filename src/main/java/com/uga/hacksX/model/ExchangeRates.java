package com.uga.hacksX.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRates {
    private String baseCurrency;
    private Map<String, Double> rates;
    
    public Double getRate(String currency) {
        return rates.getOrDefault(currency, 1.0);
    }
} 