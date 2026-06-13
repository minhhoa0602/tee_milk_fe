package com.example.myapplication.model.order;
import java.util.List;

public class OrderRequest {
    private String receiverName;
    private String phoneNumber;
    private int addressId;
    private List<Integer> selectedCartItemIds;
    private String payment; // "CASH" hoặc "MOMO"
    private String note;

    public OrderRequest(String receiverName, String phoneNumber, int addressId, List<Integer> selectedCartItemIds, String payment, String note) {
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.addressId = addressId;
        this.selectedCartItemIds = selectedCartItemIds;
        this.payment = payment;
        this.note = note;
    }
}