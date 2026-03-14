package com.example.nutritionservice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.nutritionservice.model.Food;
import com.example.nutritionservice.repository.FoodRepository;

@RestController
@RequestMapping("/foods")
public class FoodController {

    private final FoodRepository repository;

    public FoodController(FoodRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Food> retrieveAllFoods() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> retrieveOneFood(@PathVariable long id) {
        Optional<Food> food = repository.findById(id);

        if (food.isEmpty()) {
            System.out.println("Food not found in database");
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(food.get());
        }
    }

    @PostMapping
    public ResponseEntity<Food> createFood(@RequestBody Food food) {
        Food savedFood = repository.save(food);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFood);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(@PathVariable long id, @RequestBody Food updatedFood) {
        Optional<Food> existingFood = repository.findById(id);

        if (existingFood.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Food food = existingFood.get();
        food.setFoodName(updatedFood.getFoodName());
        food.setCategory(updatedFood.getCategory());
        food.setCaloriesPerServing(updatedFood.getCaloriesPerServing());
        food.setProtein(updatedFood.getProtein());
        food.setCarbs(updatedFood.getCarbs());
        food.setFat(updatedFood.getFat());

        Food savedFood = repository.save(food);
        return ResponseEntity.ok(savedFood);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable long id) {
        Optional<Food> existingFood = repository.findById(id);

        if (existingFood.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllFoods() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}