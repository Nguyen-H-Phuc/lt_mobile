package com.example.project183.Domain;

public class OrderFood {
    private final int id;
    private final String title;
    private final double price;
    private final int quantity;
    private final String imagePath;

    public OrderFood(int id, String title, double price, int quantity,  String imagePath) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.imagePath = imagePath;
        this.quantity = quantity;
    }

    // Getters (nên có để Firebase sử dụng)
    public int getId() { return id; }
    public double getPrice() { return price; }
    public String getTitle() { return title; }
    public String getImagePath() { return imagePath; }
    public int getQuantity() { return quantity; }
}

