package com.example.myapplication.model;

import java.util.List;

public class ProductOptions {
    private Integer productId;
    private String productName;
    private List<SizeOption> sizes;
    private List<String> sugarLevels;
    private List<String> iceLevels;
    private List<ToppingOption> toppings;

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public List<SizeOption> getSizes() { return sizes; }
    public List<String> getSugarLevels() { return sugarLevels; }
    public List<String> getIceLevels() { return iceLevels; }
    public List<ToppingOption> getToppings() { return toppings; }
}
