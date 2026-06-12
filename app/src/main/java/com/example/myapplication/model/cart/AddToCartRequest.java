package com.example.myapplication.model.cart;

import java.util.List;

public class AddToCartRequest {
    private int productId;
    private int sizeId;
    private String iceLevel;
    private String sugarLevel;
    private List<Integer> toppingIds;
    private int quantity;

    public AddToCartRequest(int productId, int sizeId, String iceLevel, String sugarLevel, List<Integer> toppingIds, int quantity) {
        this.productId = productId;
        this.sizeId = sizeId;
        this.iceLevel = iceLevel;
        this.sugarLevel = sugarLevel;
        this.toppingIds = toppingIds;
        this.quantity = quantity;
    }
}
