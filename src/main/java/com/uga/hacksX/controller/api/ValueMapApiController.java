package com.uga.hacksX.controller.api;

import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import com.uga.hacksX.service.ValueCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/valumap")
@RequiredArgsConstructor
public class ValueMapApiController {
    
    private final ValueCalculationService valueCalculationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CountryValue> calculateValues(@RequestBody UserInput userInput) {
        log.info("Received calculation request for budget: {} {}", userInput.getBudget(), userInput.getBaseCurrency());
        return valueCalculationService.calculateValues(userInput);
    }

    @GetMapping("/sample")
    public UserInput getSampleInput() {
        return UserInput.builder()
                .budget(3000000)
                .baseCurrency("KRW")
                .travelStyle("STANDARD")
                .build();
    }
} 