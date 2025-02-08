package com.uga.hacksX.controller.web;

import com.uga.hacksX.model.UserInput;
import com.uga.hacksX.service.ValueCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/valumap")
@RequiredArgsConstructor
public class ValueMapController {

    private final ValueCalculationService valueCalculationService;

    @GetMapping
    public String showValueMapForm(Model model) {
        model.addAttribute("userInput", new UserInput());
        return "valumap/form";
    }

    @PostMapping
    public String calculateValueMap(@ModelAttribute UserInput userInput, Model model) {
        log.info("Processing value map calculation for budget: {} {}", 
                userInput.getBudget(), userInput.getBaseCurrency());
        
        model.addAttribute("userInput", userInput);
        valueCalculationService.calculateValues(userInput)
                .collectList()
                .subscribe(values -> model.addAttribute("countryValues", values));
        
        return "valumap/result";
    }
} 