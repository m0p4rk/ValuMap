package com.uga.hacksX.service;

import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import reactor.core.publisher.Flux;

public interface ValueCalculationService {
    Flux<CountryValue> calculateValues(UserInput userInput);
}
