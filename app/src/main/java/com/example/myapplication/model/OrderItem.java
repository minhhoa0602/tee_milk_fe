package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderItem {
    private int id;
    @SerializedName("product_id")
    private int productId;
    @SerializedName("product_name")
    private String productName;
    @SerializedName("product_image")
    private String productImage;
    @SerializedName("size_name")
    private String sizeName;
    @SerializedName("ice_level")
    private String iceLevel;
    @SerializedName("sugar_level")
    private String sugarLevel;
    private int quantity;
    @SerializedName("unit_price")
    private double unitPrice;
    private List<OrderItemTopping> toppings;

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductImage() { return productImage; }
    public String getSizeName() { return sizeName; }
    public String getIceLevel() { return iceLevel; }
    public String getSugarLevel() { return sugarLevel; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public List<OrderItemTopping> getToppings() { return toppings; }
}
