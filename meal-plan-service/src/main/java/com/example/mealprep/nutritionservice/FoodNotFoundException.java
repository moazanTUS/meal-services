package com.example.mealprep.nutritionservice;

public class FoodNotFoundException extends RuntimeException {

    public FoodNotFoundException(long foodId) {
        super("Food not found for id " + foodId);
    }
}
