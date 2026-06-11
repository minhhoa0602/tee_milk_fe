package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("total_amount")
    private double totalAmount;
    @SerializedName("payment_method")
    private String paymentMethod;
    private String status;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("phone_number")
    private String phoneNumber;
    private String note;
    @SerializedName("order_items")
    private List<OrderItem> orderItems;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getReceiverName() { return receiverName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getNote() { return note; }
    public List<OrderItem> getOrderItems() { return orderItems; }
}
