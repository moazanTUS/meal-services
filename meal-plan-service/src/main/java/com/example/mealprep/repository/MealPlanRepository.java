package com.example.mealprep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mealprep.model.MealPlan;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
}