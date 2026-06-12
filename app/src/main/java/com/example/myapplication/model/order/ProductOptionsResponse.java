package com.example.myapplication.model.order;

import com.example.myapplication.model.Size;
import com.example.myapplication.model.Topping;

import java.math.BigDecimal;
import java.util.List;

public class ProductOptionsResponse {
    private int productId; // 🔥 Đã đổi sang int vì BE đã trả về ID chuẩn
    private String productName;
    private String imageUrl;
    private BigDecimal basePrice;

    private List<Size> sizes;
    private List<String> iceLevels;
    private List<String> sugarLevels;
    private List<Topping> toppings;
    public Integer getProductId() {
        return productId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Size> getSizes() {
        return sizes;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
    }

    public List<String> getSugarLevels() {
        return sugarLevels;
    }

    public void setSugarLevels(List<String> sugarLevels) {
        this.sugarLevels = sugarLevels;
    }

    public List<String> getIceLevels() {
        return iceLevels;
    }

    public void setIceLevels(List<String> iceLevels) {
        this.iceLevels = iceLevels;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    public void setToppings(List<Topping> toppings) {
        this.toppings = toppings;
    }
}
