package com.example.myapplication.model;

import java.math.BigDecimal;

public class ToppingOption {
    private Integer id;
    private String name;
    private BigDecimal price;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price != null ? price : BigDecimal.ZERO; }
}
