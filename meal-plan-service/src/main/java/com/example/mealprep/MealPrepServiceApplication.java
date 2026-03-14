package com.example.mealprep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MealPrepServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MealPrepServiceApplication.class, args);
    }
}