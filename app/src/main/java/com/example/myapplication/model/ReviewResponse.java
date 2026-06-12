package com.example.myapplication.model;

public class ReviewResponse {
    private Integer id;
    private String userName;
    private Integer ratingStar;
    private String comment;
    private String imageUrl;
    private String createdAt;

    public Integer getId() { return id; }
    public String getUserName() { return userName; }
    public Integer getRatingStar() { return ratingStar; }
    public String getComment() { return comment; }
    public String getImageUrl() { return imageUrl; }
    public String getCreatedAt() { return createdAt; }
}