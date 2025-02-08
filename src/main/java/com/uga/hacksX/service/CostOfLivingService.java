package com.uga.hacksX.service;

import com.uga.hacksX.model.CountryCost;
import java.util.List;

public interface CostOfLivingService {
    CountryCost getCountryCost(String countryCode);
    List<CountryCost> getAllCountryCosts();
}
