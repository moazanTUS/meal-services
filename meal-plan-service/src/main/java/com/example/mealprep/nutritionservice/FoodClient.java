package com.example.mealprep.nutritionservice;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FoodClient {

    private final RestClient restClient;

    public FoodClient(RestClient.Builder restClientBuilder,
            @Value("${nutritionservice.service.url}") String nutritionServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = restClientBuilder
                .baseUrl(nutritionServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Async("foodClientExecutor")
    public CompletableFuture<Food> getFoodByIdAsync(long id) {
        return CompletableFuture.completedFuture(fetchFoodById(id));
    }

    public Food getFoodById(long id) {
        return fetchFoodById(id);
    }

    public ResponseEntity<List<Food>> getAllFoods() {
        return fetchAllFoods();
    }

    public Food createFood(Food food) {
        return executeWithErrorTranslation(() -> restClient.post()
                .uri("/foods")
                .body(food)
                .retrieve()
                .body(Food.class));
    }

    public Food updateFood(long id, Food food) {
        return executeWithErrorTranslation(() -> restClient.put()
                .uri("/foods/{id}", id)
                .body(food)
                .retrieve()
                .body(Food.class), id);
    }

    public void deleteFoodById(long id) {
        executeWithErrorTranslation(() -> {
            restClient.delete()
                    .uri("/foods/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        }, id);
    }

    private Food fetchFoodById(long id) {
        return executeWithErrorTranslation(() -> restClient.get()
                .uri("/foods/{id}", id)
                .retrieve()
                .body(Food.class), id);
    }

    private ResponseEntity<List<Food>> fetchAllFoods() {
        return executeWithErrorTranslation(() -> restClient.get()
                .uri("/foods")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Food>>() {
                }));
    }

    private <T> T executeWithErrorTranslation(ThrowingSupplier<T> supplier) {
        return executeWithErrorTranslation(supplier, null);
    }

    private <T> T executeWithErrorTranslation(ThrowingSupplier<T> supplier, Long foodId) {
        try {
            return supplier.get();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404 && foodId != null) {
                throw new FoodNotFoundException(foodId);
            }
            throw new IllegalStateException("Nutrition service request failed with status " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException("Nutrition service is unavailable", ex);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get();
    }
}
