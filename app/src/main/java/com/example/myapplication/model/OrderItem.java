package com.example.myapplication.model;

// Matches backend OrderItemResponse DTO
public class OrderItem {
    private int id;
    private int productId;
    private String productName;
    private int quantity;
    private String productImageUrl;
    private double unitPrice;

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getProductImageUrl() { return productImageUrl; }
    public double getUnitPrice() { return unitPrice; }

    // Toppings không có trong OrderItemResponse của backend hiện tại
    public java.util.List<OrderItemTopping> getToppings() { return null; }
}
