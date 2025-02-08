package com.uga.hacksX.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInput {
    private double budget;
    private String baseCurrency = "KRW";
    private String travelStyle = "STANDARD"; // BACKPACKER, STANDARD, LUXURY
    
    // 여행 스타일에 따른 가중치 계산
    public double getTravelStyleMultiplier() {
        return switch (travelStyle.toUpperCase()) {
            case "BACKPACKER" -> 0.6;
            case "LUXURY" -> 2.0;
            default -> 1.0;
        };
    }
}
