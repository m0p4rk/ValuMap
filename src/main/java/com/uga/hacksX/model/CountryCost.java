package com.uga.hacksX.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryCost {
    private String countryCode;
    private String countryName;
    private String currency;
    private double dailyCostStandard;    // 일반적인 여행 스타일 기준 일일 비용
    private double foodCost;             // 식비
    private double accommodationCost;     // 숙박비
    private double transportationCost;    // 교통비
    
    public double calculateAdjustedDailyCost(String travelStyle) {
        return dailyCostStandard * switch (travelStyle.toUpperCase()) {
            case "BACKPACKER" -> 0.6;
            case "LUXURY" -> 2.0;
            default -> 1.0;
        };
    }
} 