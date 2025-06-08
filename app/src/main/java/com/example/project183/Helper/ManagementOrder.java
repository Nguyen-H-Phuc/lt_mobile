package com.example.project183.Helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.project183.Activity.OrderSuccessActivity;
import com.example.project183.Domain.OrderFood;
import com.example.project183.service.UserAuthService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class ManagementOrder {
    private final DatabaseReference ordersRef;
    private final UserAuthService userAuthService;

    public ManagementOrder() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");
        userAuthService = new UserAuthService();
    }

    public void sendOrderToFireBase(Context context, String address, List<OrderFood> orderFoods, double totalListFood, double deliveryFee, int deliveryTime, double totalPrice) {
        String orderId = ordersRef.push().getKey();
        String uid = userAuthService.getCurrentUser().getUid();
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("userId", uid);
        orderData.put("orderId", orderId);
        orderData.put("address", address);
        orderData.put("orderFoods", orderFoods);
        orderData.put("totalListFood", totalListFood);
        orderData.put("deliverFee", deliveryFee);
        orderData.put("deliverTime", deliveryTime);
        orderData.put("totalPrice", totalPrice);
        orderData.put("timestamp", System.currentTimeMillis());
        orderData.put("status", "Chở xác nhận");

        if (orderId != null) {
            ordersRef.child(orderId).setValue(orderData)
                .addOnSuccessListener(aVoid -> {
                    new ManagmentCart(context).clearCart();
                    Log.d("Firebase", "Đơn hàng đã được gửi.");
                    Intent intent = new Intent(context, OrderSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Nếu gọi từ ViewModel hoặc lớp không phải Activity
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Gửi đơn hàng thất bại" + e.getMessage());
                });
        }
    }

}
