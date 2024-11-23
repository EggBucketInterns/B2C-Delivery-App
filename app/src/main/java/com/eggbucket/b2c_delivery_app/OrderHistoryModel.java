package com.eggbucket.b2c_delivery_app; // Use your actual package name

import java.util.List;

public class OrderHistoryModel {
    private String date;
    private String status;
    private String id;
    private String amount;
    private List<Integer> productImages; // List of drawable resource IDs for images

    // Constructor
    public OrderHistoryModel(String date, String status, String id, String amount, List<Integer> productImages) {
        this.date = date;
        this.status = status;
        this.id = id;
        this.amount = amount;
        this.productImages = productImages;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public List<Integer> getProductImages() {
        return productImages;
    }
}
