package com.example.myapplication.model;

import java.util.List;

public class Order {
    // Backend trả orderCode dạng "#MT00000001", orderDate, orderStatus, totalAmount, orderItems
    private String orderCode;
    private String orderDate;
    private String orderStatus;
    private double totalAmount;
    private List<OrderItem> orderItems;

    public String getOrderCode() { return orderCode; }
    public String getOrderDate() { return orderDate; }
    public String getOrderStatus() { return orderStatus; }
    public double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getOrderItems() { return orderItems; }

    // Tiện ích: trích xuất ID số từ orderCode "#MT00000001" -> 1
    public int getNumericId() {
        if (orderCode == null) return 0;
        try {
            return Integer.parseInt(orderCode.replace("#MT", "").replaceAll("^0+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Alias để tương thích với code cũ dùng getStatus()
    public String getStatus() { return orderStatus; }
}
