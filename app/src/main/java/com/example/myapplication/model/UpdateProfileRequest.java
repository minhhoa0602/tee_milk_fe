package com.example.myapplication.model;

public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;

    public UpdateProfileRequest(String fullName, String phoneNumber, String avatarUrl) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
    }

    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
}
