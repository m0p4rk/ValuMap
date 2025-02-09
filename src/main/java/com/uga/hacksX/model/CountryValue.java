package com.uga.hacksX.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryValue {
    private String countryCode;      // Country code (e.g., KR, US)
    private String countryName;      // Country name
    private String localName;        // Added field
    private String currency;         // Currency code (e.g., KRW, USD)
    private String flagUrl;          // Flag image URL
    private double exchangeRate;     // Exchange rate
    private double dailyCost;        // Average daily cost
    private double valueScore;       // Value score (0-100)
    private int possibleDays;        // Possible travel days with given budget
    private String region;           // Region (e.g., East Asia & Pacific)
    private String valueGrade;       // Added field
    
    // 지도 시각화를 위한 색상 코드
    public String getColorCode() {
        if (valueScore >= 90) return "#28a745";
        if (valueScore >= 80) return "#17a2b8";
        if (valueScore >= 70) return "#ffc107";
        if (valueScore >= 65) return "#fd7e14";
        return "#dc3545";
    }
    
    // valueScore를 기반으로 등급을 계산
    public String getValueGrade() {
        if (valueScore >= 90) return "Excellent";
        if (valueScore >= 80) return "Very Good";
        if (valueScore >= 70) return "Good";
        if (valueScore >= 65) return "Fair";
        return "Average";
    }
}
