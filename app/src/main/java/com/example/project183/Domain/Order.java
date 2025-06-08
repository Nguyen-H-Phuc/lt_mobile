package com.example.project183.Domain;

import java.util.List;

public class Order {
    private String address;
    private String status;
    private String orderId;
    private long timestamp;
    private String userId;
    private double totalPrice;
    private List<Foods> orderFoods;

    private double deliverTime;

    public double getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(double deliverTime) {
        this.deliverTime = deliverTime;
    }

    // --- SỬA ĐỔI ---
    private double deliverFee; // Đổi tên từ 'deliveryFee'

    public Order() {}

    // --- SỬA ĐỔI ---
    public double getDeliverFee() { // Đổi tên phương thức
        return deliverFee;
    }

    public void setDeliverFee(double deliverFee) { // Đổi tên phương thức
        this.deliverFee = deliverFee;
    }
    // --- KẾT THÚC SỬA ĐỔI ---

    // Các Getters/Setters cũ giữ nguyên
    public List<Foods> getOrderFoods() { return orderFoods; }
    public void setOrderFoods(List<Foods> orderFoods) { this.orderFoods = orderFoods; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}