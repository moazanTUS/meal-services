package com.example.mealprep.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_plans")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "meal_type", nullable = false)
    private String mealType;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(nullable = false)
    private Integer servings;

    @Column(name = "plan_date", nullable = false)
    private String planDate;

    public MealPlan() {
    }

    public MealPlan(Long id, String userName, String mealType, Long foodId, Integer servings, String planDate) {
        this.id = id;
        this.userName = userName;
        this.mealType = mealType;
        this.foodId = foodId;
        this.servings = servings;
        this.planDate = planDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }
}