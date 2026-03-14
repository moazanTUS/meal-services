package com.example.mealprep.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.mealprep.model.MealPlanResponse;
import com.example.mealprep.model.MealPlan;
import com.example.mealprep.nutritionservice.Food;
import com.example.mealprep.nutritionservice.FoodClient;
import com.example.mealprep.repository.MealPlanRepository;

@RestController
@RequestMapping("/meal-plans")
public class MealPlanController {

    private final MealPlanRepository repository;
    private final FoodClient foodClient;

    public MealPlanController(MealPlanRepository repository, FoodClient foodClient) {
        this.repository = repository;
        this.foodClient = foodClient;
    }

    @GetMapping
    public List<MealPlan> retrieveAllMealPlans() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealPlanResponse> retrieveOneMealPlan(@PathVariable long id) {
        Optional<MealPlan> mealPlan = repository.findById(id);

        if (mealPlan.isEmpty()) {
            System.out.println("Meal plan not found in database");
            return ResponseEntity.notFound().build();
        } else {
            Food food = foodClient.getFoodById(mealPlan.get().getFoodId());

            MealPlanResponse response = new MealPlanResponse(
                    mealPlan.get().getId(),
                    mealPlan.get().getUserName(),
                    mealPlan.get().getMealType(),
                    mealPlan.get().getFoodId(),
                    mealPlan.get().getServings(),
                    mealPlan.get().getPlanDate(),
                    food.getFoodName(),
                    food.getCategory(),
                    food.getCaloriesPerServing(),
                    food.getProtein(),
                    food.getCarbs(),
                    food.getFat());

            return ResponseEntity.ok(response);
        }
    }

    @PostMapping
    public ResponseEntity<MealPlan> createMealPlan(@RequestBody MealPlan mealPlan) {
        MealPlan savedMealPlan = repository.save(mealPlan);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMealPlan);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealPlan> updateMealPlan(@PathVariable long id, @RequestBody MealPlan updatedMealPlan) {
        Optional<MealPlan> existingMealPlan = repository.findById(id);

        if (existingMealPlan.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MealPlan mealPlan = existingMealPlan.get();
        mealPlan.setUserName(updatedMealPlan.getUserName());
        mealPlan.setMealType(updatedMealPlan.getMealType());
        mealPlan.setFoodId(updatedMealPlan.getFoodId());
        mealPlan.setServings(updatedMealPlan.getServings());
        mealPlan.setPlanDate(updatedMealPlan.getPlanDate());

        return ResponseEntity.ok(repository.save(mealPlan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealPlan(@PathVariable long id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllMealPlans() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}