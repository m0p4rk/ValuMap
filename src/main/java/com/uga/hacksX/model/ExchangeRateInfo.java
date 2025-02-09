package com.uga.hacksX.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateInfo {
    private double rate;
    private double trend;  // 등락률
    
    // 추가 필드
    private String countryName;
    private String currencyCode;
    private String currencyName;
    private String flagUrl;
    private String baseAmount;
    private String color;
    private String lastUpdate;
    
    public String getFormattedRate() {
        return String.format("%,.2f", rate);
    }
    
    public String getTrendSymbol() {
        return trend >= 0 ? "▲" : "▼";
    }
    
    public String getTrendClass() {
        return trend >= 0 ? "trend-up" : "trend-down";
    }
    
    public String getFormattedTrend() {
        return String.format("%.2f", Math.abs(trend));
    }
} 