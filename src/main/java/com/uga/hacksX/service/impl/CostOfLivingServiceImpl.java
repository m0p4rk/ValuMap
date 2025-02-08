package com.uga.hacksX.service.impl;

import com.uga.hacksX.model.CountryCost;
import com.uga.hacksX.service.CostOfLivingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CostOfLivingServiceImpl implements CostOfLivingService {
    private Map<String, CountryCost> costsByCountry;
    private final ObjectMapper objectMapper;

    public CostOfLivingServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("static/data/mock-costs.json");
            List<CountryCost> costs = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<CountryCost>>() {}
            );
            costsByCountry = costs.stream()
                    .collect(Collectors.toMap(CountryCost::getCountryCode, cost -> cost));
            log.info("Loaded cost data for {} countries", costsByCountry.size());
        } catch (IOException e) {
            log.error("Error loading cost data: {}", e.getMessage());
            throw new RuntimeException("Failed to load cost data", e);
        }
    }

    @Override
    public CountryCost getCountryCost(String countryCode) {
        return costsByCountry.get(countryCode);
    }

    @Override
    public List<CountryCost> getAllCountryCosts() {
        return List.copyOf(costsByCountry.values());
    }
}
