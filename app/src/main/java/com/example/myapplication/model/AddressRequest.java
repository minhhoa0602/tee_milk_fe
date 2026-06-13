package com.example.myapplication.model;

public class AddressRequest {
    private String addressLine;
    private boolean isDefault;

    public AddressRequest(String addressLine, boolean isDefault) {
        this.addressLine = addressLine;
        this.isDefault = isDefault;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
