package com.uga.hacksX.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInput {
    private double budget;
    private String currency;
    private String baseCurrency;
    private String travelStyle;
    private String searchQuery;      // 국가 검색어
    private String region;           // 지역 필터
    private String sortBy;           // 정렬 기준 (가성비순/비용순/일수순)
    private Integer minDays;         // 최소 체류 일수
    private Integer maxDays;         // 최대 체류 일수
    
    public boolean isValidBudget() {
        return budget > 0;
    }

    public String getValidationMessage() {
        if (!isValidBudget()) {
            return "Please enter a valid budget amount greater than 0.";
        }
        return "";
    }

    public boolean isValidCurrency() {
        return currency != null && !currency.trim().isEmpty();
    }

    public boolean isValidTravelStyle() {
        return travelStyle != null && !travelStyle.trim().isEmpty();
    }

    public double getTravelStyleMultiplier() {
        return switch (travelStyle) {
            case "BUDGET" -> 0.7;
            case "LUXURY" -> 1.5;
            default -> 1.0;
        };
    }
}