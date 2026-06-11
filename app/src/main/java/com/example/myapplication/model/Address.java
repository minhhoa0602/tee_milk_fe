package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Address {
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("address_line")
    private String addressLine;
    @SerializedName("is_default")
    private boolean isDefault;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getAddressLine() { return addressLine; }
    public boolean isDefault() { return isDefault; }

    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
