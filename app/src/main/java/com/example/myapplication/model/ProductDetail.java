package com.example.myapplication.model;

import java.math.BigDecimal;
import java.util.List;

public class ProductDetail {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private Integer soldCount;
    private Double averageRating;
    private List<ReviewItem> reviews;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getImageUrl() { return imageUrl; }
    public Integer getSoldCount() { return soldCount != null ? soldCount : 0; }
    public Double getAverageRating() { return averageRating != null ? averageRating : 0.0; }
    public List<ReviewItem> getReviews() { return reviews; }
}
