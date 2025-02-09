package com.uga.hacksX.controller.web;

import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import com.uga.hacksX.model.ExchangeRateInfo;
import com.uga.hacksX.service.ValueCalculationService;
import com.uga.hacksX.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ValuMapController {
    private final ValueCalculationService valueCalculationService;
    private final ExchangeRateService exchangeRateService;
    
    // Store previous rates for trend calculation
    private static final Map<String, Double> previousRates = new HashMap<>() {{
        put("JPY", 0.0067);  // 100 JPY to USD
        put("USD", 1.0);     // USD to USD
        put("EUR", 1.08);    // EUR to USD
    }};

    @GetMapping("/")
    public String showForm(Model model) {
        try {
            Map<String, ExchangeRateInfo> exchangeRates = new HashMap<>();
            
            // Get exchange rates
            double jpyUsdRate = exchangeRateService.getExchangeRate("USD", "JPY").block();
            double krwUsdRate = exchangeRateService.getExchangeRate("USD", "KRW").block();
            double eurUsdRate = exchangeRateService.getExchangeRate("USD", "EUR").block();
            
            // Update exchange rates
            exchangeRates.put("JPY", ExchangeRateInfo.builder()
                    .rate(100.0 / jpyUsdRate)
                    .trend(calculateTrend("JPY", 100.0 / jpyUsdRate))
                    .build());
            
            exchangeRates.put("KRW", ExchangeRateInfo.builder()
                    .rate(1000.0 / krwUsdRate)
                    .trend(calculateTrend("KRW", 1000.0 / krwUsdRate))
                    .build());
            
            exchangeRates.put("EUR", ExchangeRateInfo.builder()
                    .rate(eurUsdRate)
                    .trend(calculateTrend("EUR", eurUsdRate))
                    .build());

            model.addAttribute("exchangeRates", exchangeRates);
            
        } catch (Exception e) {
            log.error("Error fetching exchange rates: {}", e.getMessage());
            model.addAttribute("error", "Failed to fetch exchange rates. Using default values.");
        }
        
        model.addAttribute("userInput", new UserInput());
        return "valumap/form";
    }

    @PostMapping("/calculate")
    public String calculateValues(@ModelAttribute UserInput userInput, Model model) {
        if (!isValidInput(userInput)) {
            model.addAttribute("error", "Please enter valid input values.");
            return "valumap/form";
        }

        setDefaultValues(userInput);

        try {
            List<CountryValue> results = valueCalculationService.calculateValues(userInput)
                    .collectList()
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (results == null || results.isEmpty()) {
                model.addAttribute("error", "No results found. Please try again.");
                return "valumap/form";
            }

            model.addAttribute("results", results);
            model.addAttribute("userInput", userInput);
            return "valumap/results";

        } catch (Exception e) {
            log.error("Error during calculation: {}", e.getMessage());
            model.addAttribute("error", "An error occurred during calculation.");
            return "valumap/error";
        }
    }

    @GetMapping("/results")
    public String showResults() {
        return "valumap/results";
    }

    @GetMapping("/sample")
    public String showSample(Model model) {
        UserInput sampleInput = UserInput.builder()
                .budget(3000000)
                .baseCurrency("KRW")
                .travelStyle("STANDARD")
                .build();
        
        model.addAttribute("userInput", sampleInput);
        return "valumap/form";
    }

    @GetMapping("/home")
    public String redirectToHome() {
        return "redirect:/";
    }

    @GetMapping("/index")
    public String redirectToIndex() {
        return "redirect:/";
    }

    @GetMapping("/exchange-rates")
    public String showExchangeRates(Model model) {
        try {
            List<ExchangeRateInfo> allRates = new ArrayList<>();
            
            // Get and format exchange rates
            double jpyUsdRate = exchangeRateService.getExchangeRate("USD", "JPY").block();
            double krwUsdRate = exchangeRateService.getExchangeRate("USD", "KRW").block();
            double eurUsdRate = exchangeRateService.getExchangeRate("USD", "EUR").block();
            
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            // Add JPY
            allRates.add(ExchangeRateInfo.builder()
                    .countryName("Japan")
                    .currencyCode("JPY")
                    .currencyName("Japanese Yen")
                    .flagUrl("https://flagcdn.com/w80/jp.png")
                    .rate(jpyUsdRate)
                    .baseAmount("100")
                    .trend(calculateTrend("JPY", jpyUsdRate))
                    .color("#e91e63")
                    .lastUpdate(currentTime)
                    .build());
            
            // Add KRW
            allRates.add(ExchangeRateInfo.builder()
                    .countryName("South Korea")
                    .currencyCode("KRW")
                    .currencyName("Korean Won")
                    .flagUrl("https://flagcdn.com/w80/kr.png")
                    .rate(krwUsdRate)
                    .baseAmount("1000")
                    .trend(calculateTrend("KRW", krwUsdRate))
                    .color("#3f51b5")
                    .lastUpdate(currentTime)
                    .build());
            
            // Add EUR
            allRates.add(ExchangeRateInfo.builder()
                    .countryName("European Union")
                    .currencyCode("EUR")
                    .currencyName("Euro")
                    .flagUrl("https://flagcdn.com/w80/eu.png")
                    .rate(eurUsdRate)
                    .baseAmount("1")
                    .trend(calculateTrend("EUR", eurUsdRate))
                    .color("#4caf50")
                    .lastUpdate(currentTime)
                    .build());

            model.addAttribute("allRates", allRates);
            
        } catch (Exception e) {
            log.error("Error fetching exchange rates: {}", e.getMessage());
            model.addAttribute("error", "Failed to fetch exchange rates");
        }
        
        return "valumap/exchange-rates";
    }

    private void setDefaultValues(UserInput userInput) {
        if (userInput.getCurrency() == null || userInput.getCurrency().trim().isEmpty()) {
            userInput.setCurrency("USD");
        }
        if (userInput.getBaseCurrency() == null || userInput.getBaseCurrency().trim().isEmpty()) {
            userInput.setBaseCurrency("USD");
        }
        if (userInput.getTravelStyle() == null || userInput.getTravelStyle().trim().isEmpty()) {
            userInput.setTravelStyle("STANDARD");
        }
    }

    private boolean isValidInput(UserInput userInput) {
        return userInput != null && userInput.isValidBudget();
    }

    private double calculateTrend(String currency, double currentRate) {
        Double previousRate = previousRates.get(currency);
        if (previousRate == null || previousRate == 0) {
            return 0.0;
        }
        return Math.round(((currentRate - previousRate) / previousRate * 100.0) * 100.0) / 100.0;
    }
} 