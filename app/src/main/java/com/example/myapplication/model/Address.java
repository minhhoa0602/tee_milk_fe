package com.example.myapplication.model;

// Matches backend AddressResponse DTO (camelCase)
public class Address {
    private int id;
    private String addressLine;
    private Boolean isDefault;

    public int getId() { return id; }
    public String getAddressLine() { return addressLine; }
    public boolean isDefault() { return Boolean.TRUE.equals(isDefault); }

    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
