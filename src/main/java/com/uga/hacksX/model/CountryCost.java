package com.uga.hacksX.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryCost {
    private String countryCode;
    private String countryName;
    private String currency;
    private Map<String, Double> costs;    // 여행 스타일별 비용 맵
} 