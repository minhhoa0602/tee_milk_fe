package com.example.myapplication.model;

// Matches backend UserProfileResponse DTO (camelCase fields)
public class UserProfile {
    private int id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String defaultAddress;

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getDefaultAddress() { return defaultAddress; }

    // Alias để tương thích với code cũ dùng getPhone()
    public String getPhone() { return phoneNumber; }
}
