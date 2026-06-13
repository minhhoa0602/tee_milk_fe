package com.example.myapplication.model;

import java.math.BigDecimal;
import java.util.List;

//  ProductDetailResponse cua BE
public class ProductDetailResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private Integer soldCount;
    private Double averageRating;
    private List<ReviewResponse> reviews;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getImageUrl() { return imageUrl; }
    public Integer getSoldCount() { return soldCount; }
    public Double getAverageRating() { return averageRating; }
    public List<ReviewResponse> getReviews() { return reviews; }
}