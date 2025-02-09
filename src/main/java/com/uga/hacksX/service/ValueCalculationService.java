package com.uga.hacksX.service;

import com.uga.hacksX.model.CountryValue;
import com.uga.hacksX.model.UserInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface ValueCalculationService {
    Flux<CountryValue> calculateValues(UserInput userInput);
    Mono<CountryValue> calculateValueForCountry(String countryCode, UserInput userInput);
    Mono<List<CountryValue>> getPopularDestinations();
    Flux<CountryValue> searchCountries(String query, UserInput userInput);
}
