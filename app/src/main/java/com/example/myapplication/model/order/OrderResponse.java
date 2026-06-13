package com.example.myapplication.model.order;
import java.math.BigDecimal;

public class OrderResponse {
    private Integer orderId;
    private String paymentMethod;
    private String qrCodeUrl; // Link MoMo
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getOrderId() { return orderId; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}