package com.example.project183.Domain;
public class FoodDomain {
    private String title;
    private String description;

    public FoodDomain(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
