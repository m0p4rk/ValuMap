package com.uga.hacksX.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uga.hacksX.model.CountryCost;
import com.uga.hacksX.service.CostOfLivingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class CostOfLivingServiceImpl implements CostOfLivingService {

    private List<CountryCost> countryCosts;
    private final ObjectMapper objectMapper;

    public CostOfLivingServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = new ClassPathResource("static/data/mock-costs.json");
            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
            JsonNode countriesNode = rootNode.get("countries");
            
            countryCosts = objectMapper.convertValue(countriesNode, 
                new TypeReference<List<CountryCost>>() {});
            
            log.info("Loaded {} country costs", countryCosts.size());
        } catch (Exception e) {
            log.error("Error loading cost data: {}", e.getMessage());
            throw new RuntimeException("Failed to load cost data", e);
        }
    }

    @Override
    public Flux<CountryCost> getAllCosts() {
        return Flux.fromIterable(countryCosts);
    }

    @Override
    public Mono<CountryCost> getCostByCountryCode(String countryCode) {
        return Mono.justOrEmpty(countryCosts.stream()
                .filter(cost -> cost.getCountryCode().equals(countryCode))
                .findFirst());
    }
}
