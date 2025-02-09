package com.uga.hacksX.model.worldbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldBankIndicator {
    private String countryCode;      // ISO 국가 코드 (예: KR, US)
    private String countryName;      // 국가명 (예: South Korea, United States)
    private String region;           // 지역 (예: East Asia & Pacific)
    private String currency;         // 통화 코드 (예: KRW, USD)
    private Double gdpPpp;            // GDP per capita, PPP
    private Double cpi;               // Consumer Price Index
    private String indicatorCode;
    private String indicatorName;
    private String date;
    private Double value;

    public boolean isValid() {
        return countryCode != null && !countryCode.isEmpty() &&
               gdpPpp != null && cpi != null &&
               currency != null && !currency.isEmpty();
    }

    public String getFormattedValue() {
        if (gdpPpp != null) {
            return String.format("$%,.2f", gdpPpp);
        }
        return String.format("%.2f", gdpPpp);
    }

    public String getDescription() {
        if (countryName == null || countryName.isEmpty()) {
            return "Country Information";
        }
        return String.format("%s (%s)", countryName, region);
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %s", 
            countryCode, 
            countryName != null ? countryName : "Country Info",
            getFormattedValue());
    }
} 