package com.uga.hacksX.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class AppConfig {
    private String[] countries;
    private String baseCurrency;
    
    public String[] getSupportedCountries() {
        return countries != null ? countries : new String[]{"KR", "JP", "US", "FR", "TH"};
    }
    
    public String getBaseCurrency() {
        return baseCurrency != null ? baseCurrency : "KRW";
    }
} 