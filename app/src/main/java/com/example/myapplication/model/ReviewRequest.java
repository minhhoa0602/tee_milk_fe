package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("order_id")
    private int orderId;
    @SerializedName("product_id")
    private int productId;
    @SerializedName("rating_star")
    private int ratingStar;
    private String comment;
    @SerializedName("image_url")
    private String imageUrl;

    public ReviewRequest(int orderId, int productId, int ratingStar, String comment, String imageUrl) {
        this.orderId = orderId;
        this.productId = productId;
        this.ratingStar = ratingStar;
        this.comment = comment;
        this.imageUrl = imageUrl;
    }

    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public int getRatingStar() { return ratingStar; }
    public String getComment() { return comment; }
    public String getImageUrl() { return imageUrl; }
}
