package com.example.myapplication.model.cart;

import java.util.List;

public class UpdateCartRequest {
    private int cartItemId;
    private int sizeId;
    private String iceLevel;
    private String sugarLevel;
    private List<Integer> toppingIds;
    private int quantity;

    public UpdateCartRequest(int cartItemId, int sizeId, String iceLevel, String sugarLevel, List<Integer> toppingIds, int quantity) {
        this.cartItemId = cartItemId;
        this.sizeId = sizeId;
        this.iceLevel = iceLevel;
        this.sugarLevel = sugarLevel;
        this.toppingIds = toppingIds;
        this.quantity = quantity;
    }
}
