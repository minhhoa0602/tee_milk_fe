package com.example.myapplication.model;

import java.math.BigDecimal;

public class SizeOption {
    private Integer id;
    private String name;
    private BigDecimal priceAdd;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPriceAdd() { return priceAdd != null ? priceAdd : BigDecimal.ZERO; }
}
