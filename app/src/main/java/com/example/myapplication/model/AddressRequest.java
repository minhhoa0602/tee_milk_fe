package com.example.myapplication.model;

public class AddressRequest {
    private String addressLine;
    private Boolean isDefault;

    public AddressRequest(String addressLine, Boolean isDefault) {
        this.addressLine = addressLine;
        this.isDefault = isDefault;
    }

    public String getAddressLine() { return addressLine; }
    public Boolean getIsDefault() { return isDefault; }
}
