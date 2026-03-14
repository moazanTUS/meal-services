package com.example.mealprep.nutritionservice;

public class Food {

    private Long id;
    private String foodName;
    private String category;
    private Integer caloriesPerServing;
    private Integer protein;
    private Integer carbs;
    private Integer fat;

    public Food() {
    }

    public Food(Long id, String foodName, String category, Integer caloriesPerServing,
                Integer protein, Integer carbs, Integer fat) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Food [id=" + id + ", foodName=" + foodName + ", category=" + category
                + ", caloriesPerServing=" + caloriesPerServing + ", protein=" + protein
                + ", carbs=" + carbs + ", fat=" + fat + "]";
    }
}