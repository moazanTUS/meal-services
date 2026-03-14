package com.example.nutritionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.nutritionservice.model.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

}