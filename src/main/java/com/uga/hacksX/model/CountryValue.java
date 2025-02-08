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
    private String countryCode;      // ISO 국가 코드 (예: KR, US)
    private String countryName;      // 국가명
    private String currency;         // 통화 코드 (예: KRW, USD)
    private double exchangeRate;     // 환율
    private double dailyCost;        // 일일 평균 비용
    private double valueScore;       // 가성비 점수
    private int possibleDays;        // 주어진 예산으로 여행 가능한 일수
    
    // 지도 시각화를 위한 색상 범위 계산
    public String getColorCode() {
        // valueScore에 따라 색상 코드 반환 (예: #FF0000)
        if (valueScore >= 80) return "#00FF00";      // 높은 가성비
        else if (valueScore >= 60) return "#90EE90";
        else if (valueScore >= 40) return "#FFFF00";
        else if (valueScore >= 20) return "#FFA500";
        else return "#FF0000";                       // 낮은 가성비
    }
}
