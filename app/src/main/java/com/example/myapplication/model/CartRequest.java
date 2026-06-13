package com.example.myapplication.model;

import java.util.List;

public class CartRequest {
    private Integer productId;
    private Integer sizeId;
    private String iceLevel;
    private String sugarLevel;
    private List<Integer> toppingIds;
    private Integer quantity;

    public CartRequest(Integer productId, Integer sizeId, String iceLevel,
                       String sugarLevel, List<Integer> toppingIds, Integer quantity) {
        this.productId = productId;
        this.sizeId = sizeId;
        this.iceLevel = iceLevel;
        this.sugarLevel = sugarLevel;
        this.toppingIds = toppingIds;
        this.quantity = quantity;
    }
}
