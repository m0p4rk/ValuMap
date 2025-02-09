package com.uga.hacksX.controller.api;

import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import com.uga.hacksX.service.ValueCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/valumap")
@RequiredArgsConstructor
public class ValueMapApiController {
    
    private final ValueCalculationService valueCalculationService;

    @PostMapping("/countries")
    public Flux<CountryValue> calculateValues(@RequestBody UserInput userInput) {
        log.info("Received calculation request for budget: {} {}", userInput.getBudget(), userInput.getBaseCurrency());
        return valueCalculationService.calculateValues(userInput);
    }

    @GetMapping("/coordinates")
    public Map<String, double[]> getCountryCoordinates() {
        return COUNTRY_COORDINATES;
    }

    private static final Map<String, double[]> COUNTRY_COORDINATES = new HashMap<>() {{
        // East Asia
        put("KR", new double[]{37.5665, 126.9780});
        put("JP", new double[]{35.6762, 139.6503});
        put("CN", new double[]{35.8617, 104.1954});
        put("HK", new double[]{22.3193, 114.1694});
        put("TW", new double[]{23.6978, 120.9605});
        
        // Southeast Asia
        put("SG", new double[]{1.3521, 103.8198});
        put("TH", new double[]{13.7563, 100.5018});
        put("VN", new double[]{14.0583, 108.2772});
        put("MY", new double[]{4.2105, 101.9758});
        put("ID", new double[]{-0.7893, 113.9213});
        put("PH", new double[]{12.8797, 121.7740});
        
        // Europe
        put("GB", new double[]{55.3781, -3.4360});
        put("FR", new double[]{46.2276, 2.2137});
        put("DE", new double[]{51.1657, 10.4515});
        put("IT", new double[]{41.8719, 12.5674});
        put("ES", new double[]{40.4637, -3.7492});
        put("PT", new double[]{39.3999, -8.2245});
        put("GR", new double[]{39.0742, 21.8243});
        
        // Oceania
        put("AU", new double[]{-25.2744, 133.7751});
        put("NZ", new double[]{-40.9006, 174.8860});
    }};

    @GetMapping("/sample")
    public UserInput getSampleInput() {
        return UserInput.builder()
                .budget(3000000)
                .baseCurrency("KRW")
                .travelStyle("STANDARD")
                .build();
    }
} 