package com.example.mealprep.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.mealprep.model.MealPlanResponse;
import com.example.mealprep.model.MealPlan;
import com.example.mealprep.nutritionservice.Food;
import com.example.mealprep.nutritionservice.FoodClient;
import com.example.mealprep.nutritionservice.FoodNotFoundException;
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
    public ResponseEntity<List<MealPlan>> retrieveAllMealPlans(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        List<MealPlan> mealPlans = repository.findAll();
        String currentEtag = generateEtagForMealPlans(mealPlans);

        if (ifNoneMatch != null && isEtagMatch(ifNoneMatch, currentEtag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(currentEtag).build();
        }

        return ResponseEntity.ok().eTag(currentEtag).body(mealPlans);
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<MealPlanResponse>> retrieveOneMealPlan(@PathVariable long id) {
        Optional<MealPlan> mealPlan = repository.findById(id);

        if (mealPlan.isEmpty()) {
            System.out.println("Meal plan not found in database");
            return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
        }

        return foodClient.getFoodByIdAsync(mealPlan.get().getFoodId())
                .thenApply(food -> ResponseEntity.ok(buildMealPlanResponse(mealPlan.get(), food)))
                .exceptionally(ex -> handleFoodServiceFailure(mealPlan.get().getFoodId(), ex));
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

    @GetMapping("/foods")
    public ResponseEntity<List<Food>> getAllFoods() {
        try {
            return foodClient.getAllFoods();
        } catch (IllegalStateException ex) {
            System.err.println("Error fetching foods from nutrition service: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/foods/{foodId}")
    public ResponseEntity<Food> getFood(@PathVariable long foodId) {
        try {
            return ResponseEntity.ok(foodClient.getFoodById(foodId));
        } catch (FoodNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PostMapping("/foods")
    public ResponseEntity<Food> createFood(@RequestBody Food food) {
        try {
            Food created = foodClient.createFood(food);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException ex) {
            System.err.println("Error creating food: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PutMapping("/foods/{foodId}")
    public ResponseEntity<Food> updateFood(@PathVariable long foodId, @RequestBody Food food) {
        try {
            return ResponseEntity.ok(foodClient.updateFood(foodId, food));
        } catch (FoodNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @DeleteMapping("/foods/{foodId}")
    public ResponseEntity<Void> deleteFood(@PathVariable long foodId) {
        try {
            foodClient.deleteFoodById(foodId);
            return ResponseEntity.noContent().build();
        } catch (FoodNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    private String generateEtag(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return "\"" + Base64.getEncoder().encodeToString(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create ETag", e);
        }
    }

    private boolean isEtagMatch(String ifNoneMatch, String currentEtag) {
        String normalizedTarget = normalizeEtag(currentEtag);
        String[] tokens = ifNoneMatch.split(",");
        for (String token : tokens) {
            String candidate = normalizeEtag(token.trim());
            if (normalizedTarget.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeEtag(String etag) {
        if (etag == null) {
            return "";
        }
        String candidate = etag.trim();
        if (candidate.startsWith("W/")) {
            candidate = candidate.substring(2).trim();
        }
        if (candidate.startsWith("\"") && candidate.endsWith("\"")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        return candidate;
    }

    private String generateEtagForMealPlans(List<MealPlan> mealPlans) {
        String content = mealPlans.stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(plan -> String.join("|",
                        String.valueOf(plan.getId()),
                        String.valueOf(plan.getUserName()),
                        String.valueOf(plan.getMealType()),
                        String.valueOf(plan.getFoodId()),
                        String.valueOf(plan.getServings()),
                        String.valueOf(plan.getPlanDate())))
                .collect(Collectors.joining(";"));
        return generateEtag(content);
    }

    private MealPlanResponse buildMealPlanResponse(MealPlan mealPlan, Food food) {
        return new MealPlanResponse(
                mealPlan.getId(),
                mealPlan.getUserName(),
                mealPlan.getMealType(),
                mealPlan.getFoodId(),
                mealPlan.getServings(),
                mealPlan.getPlanDate(),
                food.getFoodName(),
                food.getCategory(),
                food.getCaloriesPerServing(),
                food.getProtein(),
                food.getCarbs(),
                food.getFat());
    }

    private ResponseEntity<MealPlanResponse> handleFoodServiceFailure(long foodId, Throwable throwable) {
        Throwable cause = unwrapCompletionException(throwable);
        System.err.println("Error fetching food " + foodId + " from nutrition service: " + cause.getMessage());

        if (cause instanceof FoodNotFoundException) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    private Throwable unwrapCompletionException(Throwable throwable) {
        if (throwable instanceof CompletionException && throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }
}
