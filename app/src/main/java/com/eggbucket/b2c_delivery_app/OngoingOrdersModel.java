package com.eggbucket.b2c_delivery_app;

import org.json.JSONObject;

public class OngoingOrdersModel {

    private String orderNumber;
    private String status;
    private String orderValue;
    private JSONObject outletInfo;  // Store outlet information as JSON
    private JSONObject deliveryAddress;  // Store delivery address as JSON
    private JSONObject products;
    private String customerId;

    // Updated constructor to initialize all fields, using JSONObject for addresses
    public OngoingOrdersModel(String orderNumber, String status, String orderValue,
                              JSONObject outletInfo, JSONObject deliveryAddress, JSONObject products, String customerID) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.orderValue = orderValue;
        this.outletInfo = outletInfo;
        this.deliveryAddress = deliveryAddress;
        this.products = products;
        this.customerId = customerID;
    }

    // Getter methods for each field
    public String getOrderNumber() {
        return orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderValue() {
        return orderValue;
    }

    public JSONObject getOutletInfo() {
        return outletInfo;
    }

    public JSONObject getDeliveryAddress() {
        return deliveryAddress;
    }

    public JSONObject getProducts() {
        return products;
    }

    public String getCustomerId() {
        return customerId;
    }
}
