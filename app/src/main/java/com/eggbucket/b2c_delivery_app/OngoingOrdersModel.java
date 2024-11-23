package com.eggbucket.b2c_delivery_app;

public class OngoingOrdersModel {
    private String orderNumber;
    private String status;
    private String orderValue;

    public OngoingOrdersModel(String orderNumber, String status, String orderValue) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.orderValue = orderValue;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderValue() {
        return orderValue;
    }
}
