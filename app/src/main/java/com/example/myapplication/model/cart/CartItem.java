package com.example.myapplication.model.cart;

import java.math.BigDecimal;
import java.util.List;

public class CartItem {
    private int cartItemId;
    private int productId;
    private String productName;
    private String productImage;
    private String productSize;
    private List<String> toppingNames; // BE trả về mảng String
    private String iceLevel;
    private String sugarLevel;
    private BigDecimal price; // Giá 1 món
    private BigDecimal totalPrice; // Giá tổng = giá 1 món * số lượng
    private int quantity;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    // Biến này để Android quản lý ô Checkbox (Mặc định khi tải về là false/chưa chọn)
    private boolean isChecked = false;

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public void setProductSize(String productSize) {
        this.productSize = productSize;
    }

    public void setToppingNames(List<String> toppingNames) {
        this.toppingNames = toppingNames;
    }

    public void setIceLevel(String iceLevel) {
        this.iceLevel = iceLevel;
    }

    public void setSugarLevel(String sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Các hàm Getter & Setter cơ bản
    public int getCartItemId() { return cartItemId; }
    public String getProductName() { return productName; }
    public String getProductImage() { return productImage; }
    public String getProductSize() { return productSize; }
    public List<String> getToppingNames() { return toppingNames; }
    public String getIceLevel() { return iceLevel; }
    public String getSugarLevel() { return sugarLevel; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}