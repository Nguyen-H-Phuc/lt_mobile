package com.example.project183.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project183.Adapter.OrderAdapter;
import com.example.project183.Adapter.OrderAdapter2;
import com.example.project183.Domain.Order;
import com.example.project183.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter2 adapter;
    private ArrayList<Order> orderList;
    private ProgressBar progressBar;
    private TextView emptyOrdersTxt;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        setupRecyclerView();
        loadOrdersFromFirebase();
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBarOrders);
        emptyOrdersTxt = findViewById(R.id.emptyOrdersTxt);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter2(orderList);
        ordersRecyclerView.setAdapter(adapter);
    }

    private void loadOrdersFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Tạo query để lấy các đơn hàng của user hiện tại
        Query userOrdersQuery = ordersRef.orderByChild("userId").equalTo(currentUserId);

        userOrdersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (orderList.isEmpty()) {
                    emptyOrdersTxt.setVisibility(View.VISIBLE);
                    ordersRecyclerView.setVisibility(View.GONE);
                } else {
                    // Sắp xếp các đơn hàng theo thời gian mới nhất lên đầu
                    Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                    emptyOrdersTxt.setVisibility(View.GONE);
                    ordersRecyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                // Xử lý lỗi
            }
        });
    }
}