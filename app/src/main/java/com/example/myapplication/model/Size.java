package com.example.myapplication.model;

import java.math.BigDecimal;

public class Size {
    private int id;
    private String name;
    private BigDecimal priceAdd;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPriceAdd() {
        return priceAdd;
    }

    public void setPriceAdd(BigDecimal priceAdd) {
        this.priceAdd = priceAdd;
    }
}
