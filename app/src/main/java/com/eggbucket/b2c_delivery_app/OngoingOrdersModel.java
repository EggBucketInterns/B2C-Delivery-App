package com.eggbucket.b2c_delivery_app;

public class OngoingOrdersModel {

    private String orderNumber;
    private String status;
    private String orderValue;
    private String outletAddress;
    private String outletName;
    private String outletPhone;
    private String deliveryAddress;
    private String products;

    // Updated constructor to initialize all fields
    public OngoingOrdersModel(String orderNumber, String status, String orderValue,
                              String outletAddress, String outletName, String outletPhone,
                              String deliveryAddress, String products) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.orderValue = orderValue;
        this.outletAddress = outletAddress;
        this.outletName = outletName;
        this.outletPhone = outletPhone;
        this.deliveryAddress = deliveryAddress;
        this.products = products;
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

    public String getOutletAddress() {
        return outletAddress;
    }

    public String getOutletName() {
        return outletName;
    }

    public String getOutletPhone() {
        return outletPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getProducts() {
        return products;
    }
}
