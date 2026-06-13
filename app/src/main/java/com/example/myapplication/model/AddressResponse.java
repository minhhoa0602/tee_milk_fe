package com.example.myapplication.model;

// Khớp với AddressResponse của BE
public class AddressResponse {
    private Integer id;
    private String addressLine;
    private Boolean isDefault;

    public Integer getId() { return id; }
    public String getAddressLine() { return addressLine; }
    public Boolean getIsDefault() { return isDefault; }
}