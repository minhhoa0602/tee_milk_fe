package com.example.myapplication.model;

import java.math.BigDecimal;

/**
 * Model nhận dữ liệu từ BE (ProductResponse của Spring Boot)
 * Các field khớp đúng với BE:
 *   id, name, basePrice, imageUrl, soldCount, categoryName
 */
public class ProductResponse {
    private Integer id;
    private String name;
    private BigDecimal basePrice;
    private String imageUrl;
    private Integer soldCount;
    private String categoryName;

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getImageUrl() { return imageUrl; }
    public Integer getSoldCount() { return soldCount; }
    public String getCategoryName() { return categoryName; }
}