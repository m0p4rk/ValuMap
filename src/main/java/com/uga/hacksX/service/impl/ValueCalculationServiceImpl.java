package com.uga.hacksX.service.impl;

import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import com.uga.hacksX.service.ValueCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValueCalculationServiceImpl implements ValueCalculationService {
    private static final Map<String, CountryInfo> COUNTRY_INFO = new HashMap<>() {{
        // East Asia
        put("JP", new CountryInfo("Japan", "일본", 130.0));
        put("KR", new CountryInfo("South Korea", "한국", 90.0));
        put("CN", new CountryInfo("China", "중국", 50.0));
        put("HK", new CountryInfo("Hong Kong", "홍콩", 120.0));
        put("TW", new CountryInfo("Taiwan", "대만", 70.0));
        
        // Southeast Asia
        put("SG", new CountryInfo("Singapore", "싱가포르", 110.0));
        put("TH", new CountryInfo("Thailand", "태국", 35.0));
        put("VN", new CountryInfo("Vietnam", "베트남", 25.0));
        put("MY", new CountryInfo("Malaysia", "말레이시아", 40.0));
        put("ID", new CountryInfo("Indonesia", "인도네시아", 30.0));
        put("PH", new CountryInfo("Philippines", "필리핀", 35.0));
        
        // Europe
        put("GB", new CountryInfo("United Kingdom", "영국", 160.0));
        put("FR", new CountryInfo("France", "프랑스", 140.0));
        put("DE", new CountryInfo("Germany", "독일", 130.0));
        put("IT", new CountryInfo("Italy", "이탈리아", 120.0));
        put("ES", new CountryInfo("Spain", "스페인", 110.0));
        put("PT", new CountryInfo("Portugal", "포르투갈", 90.0));
        put("GR", new CountryInfo("Greece", "그리스", 80.0));
        
        // Oceania
        put("AU", new CountryInfo("Australia", "호주", 150.0));
        put("NZ", new CountryInfo("New Zealand", "뉴질랜드", 130.0));
    }};

    @Override
    public Flux<CountryValue> calculateValues(UserInput userInput) {
        if (!userInput.isValidBudget()) {
            return Flux.empty();
        }

        return Flux.fromIterable(COUNTRY_INFO.entrySet())
            .map(entry -> calculateCountryValue(entry.getKey(), entry.getValue(), userInput));
    }

    @Override
    public Mono<CountryValue> calculateValueForCountry(String countryCode, UserInput userInput) {
        CountryInfo info = COUNTRY_INFO.get(countryCode);
        if (info == null) {
            return Mono.empty();
        }
        return Mono.just(calculateCountryValue(countryCode, info, userInput));
    }

    private CountryValue calculateCountryValue(String countryCode, CountryInfo info, UserInput userInput) {
        double dailyCost = info.baseDailyCost * getTravelStyleMultiplier(userInput.getTravelStyle());
        int possibleDays = (int) (userInput.getBudget() / dailyCost);
        double valueScore = calculateValueScore(possibleDays, dailyCost);
        
        return CountryValue.builder()
            .countryCode(countryCode)
            .countryName(info.englishName)
            .localName(info.localName)
            .dailyCost(dailyCost)
            .possibleDays(possibleDays)
            .valueScore(valueScore)
            .valueGrade(calculateGrade(valueScore))
            .flagUrl("https://flagcdn.com/w80/" + countryCode.toLowerCase() + ".png")
            .build();
    }

    private static class CountryInfo {
        final String englishName;
        final String localName;
        final double baseDailyCost;

        CountryInfo(String englishName, String localName, double baseDailyCost) {
            this.englishName = englishName;
            this.localName = localName;
            this.baseDailyCost = baseDailyCost;
        }
    }

    private Map<String, String> findMatchingCountries(String query) {
        String lowercaseQuery = query.toLowerCase();
        return COUNTRY_INFO.entrySet().stream()
            .filter(entry -> entry.getValue().englishName.toLowerCase().contains(lowercaseQuery) || 
                           entry.getKey().toLowerCase().contains(lowercaseQuery))
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().englishName));
    }

    @Override
    public Flux<CountryValue> searchCountries(String query, UserInput userInput) {
        return Flux.fromIterable(findMatchingCountries(query).entrySet())
            .map(entry -> calculateCountryValue(entry.getKey(), new CountryInfo(entry.getValue(), "", 0.0), userInput));
    }

    @Override
    public Mono<List<CountryValue>> getPopularDestinations() {
        List<String> popularCodes = Arrays.asList("JP", "KR", "TH", "VN", "TW");
        UserInput defaultInput = UserInput.builder()
            .budget(2000)
            .baseCurrency("USD")
            .travelStyle("STANDARD")
            .build();
            
        return Flux.fromIterable(popularCodes)
            .map(code -> calculateCountryValue(code, COUNTRY_INFO.get(code), defaultInput))
            .collectList();
    }

    private double calculateValueScore(int days, double dailyCost) {
        double daysScore;
        if (days >= 14) daysScore = 60.0;
        else if (days >= 10) daysScore = 50.0;
        else if (days >= 7) daysScore = 40.0;
        else if (days >= 5) daysScore = 30.0;
        else if (days >= 3) daysScore = 20.0;
        else daysScore = 10.0;

        double costScore;
        if (dailyCost <= 30.0) costScore = 40.0;
        else if (dailyCost <= 50.0) costScore = 35.0;
        else if (dailyCost <= 80.0) costScore = 30.0;
        else if (dailyCost <= 100.0) costScore = 25.0;
        else costScore = 20.0;

        return daysScore + costScore;
    }

    private double getTravelStyleMultiplier(String travelStyle) {
        return switch (travelStyle.toUpperCase()) {
            case "BUDGET" -> 0.7;
            case "LUXURY" -> 1.8;
            default -> 1.0;
        };
    }

    private String calculateGrade(double score) {
        if (score >= 90.0) return "Excellent";
        if (score >= 80.0) return "Very Good";
        if (score >= 70.0) return "Good";
        if (score >= 60.0) return "Fair";
        return "Average";
    }
}