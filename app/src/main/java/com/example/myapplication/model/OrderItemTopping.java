package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class OrderItemTopping {
    private int id;
    @SerializedName("topping_name")
    private String toppingName;
    @SerializedName("topping_price")
    private double toppingPrice;

    public int getId() { return id; }
    public String getToppingName() { return toppingName; }
    public double getToppingPrice() { return toppingPrice; }
}
