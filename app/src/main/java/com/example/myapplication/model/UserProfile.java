package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    private int id;
    private String email;
    @SerializedName("full_name")
    private String fullName;
    private String phone;
    private String avatar;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAvatar() { return avatar; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
