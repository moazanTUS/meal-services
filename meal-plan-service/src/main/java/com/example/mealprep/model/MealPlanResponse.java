package com.example.mealprep.model;

public class MealPlanResponse {

    private Long id;
    private String userName;
    private String mealType;
    private Long foodId;
    private Integer servings;
    private String planDate;

    private String foodName;
    private String category;
    private Integer caloriesPerServing;
    private Integer protein;
    private Integer carbs;
    private Integer fat;

    public MealPlanResponse() {
    }

    public MealPlanResponse(Long id, String userName, String mealType, Long foodId, Integer servings,
            String planDate, String foodName, String category,
            Integer caloriesPerServing, Integer protein, Integer carbs, Integer fat) {
        this.id = id;
        this.userName = userName;
        this.mealType = mealType;
        this.foodId = foodId;
        this.servings = servings;
        this.planDate = planDate;
        this.foodName = foodName;
        this.category = category;
        this.caloriesPerServing = caloriesPerServing;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
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

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getCaloriesPerServing() {
        return caloriesPerServing;
    }

    public void setCaloriesPerServing(Integer caloriesPerServing) {
        this.caloriesPerServing = caloriesPerServing;
    }

    public Integer getProtein() {
        return protein;
    }

    public void setProtein(Integer protein) {
        this.protein = protein;
    }

    public Integer getCarbs() {
        return carbs;
    }

    public void setCarbs(Integer carbs) {
        this.carbs = carbs;
    }

    public Integer getFat() {
        return fat;
    }

    public void setFat(Integer fat) {
        this.fat = fat;
    }
}