package com.example.project183.Helper;

import android.util.Log;

import com.example.project183.Domain.Foods;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class ManagementOrder {
    private final DatabaseReference ordersRef;

    public ManagementOrder() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");
    }

    public void sendOrderToFireBase(String userId,String address, List<Foods> cartList, double total, double deliveryFee) {
        String orderId = ordersRef.push().getKey();

        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("orderId", orderId);
        orderData.put("Address", address);
        orderData.put("cartList", cartList);
        orderData.put("total", total);
        orderData.put("deliveryFee", deliveryFee);
        orderData.put("timestamp", System.currentTimeMillis());
        orderData.put("Status", "Chở xác nhận");

        if (orderId != null) {
            ordersRef.child(orderId).setValue(orderData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Đơn hàng đã được gửi.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Gửi đơn hàng thất bại" + e.getMessage());
                    });
        }
    }
}
