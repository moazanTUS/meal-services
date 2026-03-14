package com.example.mealprep.nutritionservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "nutritionservice", url = "http://localhost:8081")
@FeignClient(name = "nutritionservice", url = "${nutritionservice.service.url}")
public interface FoodClient {
    @GetMapping("/foods/{id}")
    Food getFoodById(@PathVariable("id") long id);
}