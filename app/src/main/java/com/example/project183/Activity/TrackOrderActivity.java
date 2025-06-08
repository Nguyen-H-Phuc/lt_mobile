// package com.example.project183.Activity;
package com.example.project183.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project183.Adapter.OrderDetailAdapter;
import com.example.project183.Domain.Foods;
import com.example.project183.Domain.Order;
import com.example.project183.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TrackOrderActivity extends AppCompatActivity {
    private TextView textPlacedDesc, textPreparingDesc;

    private TextView addressTxt, shippingFeeTxt, deliveryTimeTxt;
    private ImageView backBtn;
    private Button contactBtn;
    private RecyclerView itemsRecyclerView;
    private ImageView iconPlaced, iconPreparing, iconOnTheWay, iconDelivered;
    private TextView textPlacedTitle, textPreparingTitle, textOnTheWayTitle, textDeliveredTitle;

    // Thêm các TextView khác cho tổng tiền, v.v... nếu layout của bạn có
    // private TextView subtotalTxt, totalTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // --- LƯU Ý: Đảm bảo tên layout của bạn là chính xác ---
        // Dựa vào các ID, tên layout có vẻ không phải là activity_track_order.xml mà bạn cung cấp
        // Tôi sẽ giả sử layout đúng được sử dụng
        setContentView(R.layout.activity_track_order); // Hãy chắc chắn đây là file layout đúng

        initViews();
        setupEventListeners();

        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails(orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchOrderDetails(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        updateOrderStatusUI(order.getStatus());

                        if (order.getOrderFoods() != null && !order.getOrderFoods().isEmpty()) {
                            setupItemsRecyclerView(order.getOrderFoods());
                        }

                        addressTxt.setText(order.getAddress());

                        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
                        shippingFeeTxt.setText(currencyFormatter.format(order.getDeliverFee()));
                        int deliveryTime = (int) order.getDeliverTime(); // Ép kiểu double sang int
                        deliveryTimeTxt.setText("~" + deliveryTime); // Hiển thị dạng ~30

                        // Cập nhật các trường tiền khác nếu có
                        // subtotalTxt.setText(...)
                        // totalTxt.setText(currencyFormatter.format(order.getTotalPrice()));
                    }
                } else {
                    Toast.makeText(TrackOrderActivity.this, "Không tìm thấy chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TrackOrderActivity", "Lỗi khi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    private void initViews() {
        textPlacedDesc = findViewById(R.id.textPlacedDesc);
        textPreparingDesc = findViewById(R.id.textPreparingDesc);
        backBtn = findViewById(R.id.backBtn);
        contactBtn = findViewById(R.id.contactBtn);
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);

        addressTxt = findViewById(R.id.addressTxt);
        shippingFeeTxt = findViewById(R.id.shippingFeeTxt);
        deliveryTimeTxt = findViewById(R.id.deliveryTimeTxt);

        iconPlaced = findViewById(R.id.iconPlaced);
        iconPreparing = findViewById(R.id.iconPreparing);
        iconOnTheWay = findViewById(R.id.iconOnTheWay);
        iconDelivered = findViewById(R.id.iconDelivered);
        textPlacedTitle = findViewById(R.id.textPlacedTitle);
        textPreparingTitle = findViewById(R.id.textPreparingTitle);
        textOnTheWayTitle = findViewById(R.id.textOnTheWayTitle);
        textDeliveredTitle = findViewById(R.id.textDeliveredTitle);

        // Khởi tạo các view khác nếu có
        // subtotalTxt = findViewById(R.id.subtotalTxt);
        // totalTxt = findViewById(R.id.totalTxt);
    }

    private void updateOrderStatusUI(String status) {
        resetAllSteps(); // Reset tất cả về trạng thái chờ

        // --- SỬA ĐỔI LOGIC HIỂN THỊ TRẠNG THÁI ---
        switch (status) {
            case "Chở xác nhận":
                // Bước 1 là hiện tại, các bước sau là chờ
                setStepUI(iconPlaced, textPlacedTitle, textPlacedDesc, false);
                break;
            case "Đang chuẩn bị":
                // Bước 1 xong, bước 2 là hiện tại
                setStepUI(iconPlaced, textPlacedTitle, textPlacedDesc, true);
                setStepUI(iconPreparing, textPreparingTitle, textPreparingDesc, false);
                break;
            case "Đang giao":
                // Bước 1, 2 xong, bước 3 là hiện tại
                setStepUI(iconPlaced, textPlacedTitle, textPlacedDesc, true);
                setStepUI(iconPreparing, textPreparingTitle, textPreparingDesc, true);
                setStepUI(iconOnTheWay, textOnTheWayTitle, null, false); // Không có mô tả cho bước này
                break;
            case "Đã giao":
                // Tất cả các bước đã hoàn thành
                setStepUI(iconPlaced, textPlacedTitle, textPlacedDesc, true);
                setStepUI(iconPreparing, textPreparingTitle, textPreparingDesc, true);
                setStepUI(iconOnTheWay, textOnTheWayTitle, null, true);
                setStepUI(iconDelivered, textDeliveredTitle, null, true);
                break;
        }
    }

    private void resetAllSteps() {
        // Reset tất cả icon về trạng thái chờ (pending)
        iconPlaced.setImageResource(R.drawable.status_pending);
        iconPreparing.setImageResource(R.drawable.status_pending);
        iconOnTheWay.setImageResource(R.drawable.status_pending);
        iconDelivered.setImageResource(R.drawable.status_pending);

        // Reset màu chữ về mặc định
        int defaultColor = ContextCompat.getColor(this, R.color.dark_blue);
        textPlacedTitle.setTextColor(defaultColor);
        textPreparingTitle.setTextColor(defaultColor);
        textOnTheWayTitle.setTextColor(defaultColor);
        textDeliveredTitle.setTextColor(defaultColor);

        // Reset màu chữ mô tả
        int descColor = ContextCompat.getColor(this, R.color.grey);
        textPlacedDesc.setTextColor(descColor);
        textPreparingDesc.setTextColor(descColor);
    }

    // Thêm một TextView desc vào tham số để có thể thay đổi cả màu của nó
    private void setStepUI(ImageView icon, TextView title, TextView desc, boolean isDone) {
        if (isDone) {
            // Trạng thái đã hoàn thành
            icon.setImageResource(R.drawable.status_done);
            title.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
            if (desc != null) {
                desc.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
            }
        } else {
            // Trạng thái hiện tại
            icon.setImageResource(R.drawable.status_current);
            title.setTextColor(ContextCompat.getColor(this, R.color.red));
            if (desc != null) {
                desc.setTextColor(ContextCompat.getColor(this, R.color.red));
            }
        }
    }

    // Các hàm còn lại không thay đổi
    private void setupItemsRecyclerView(List<Foods> foodList) {
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        OrderDetailAdapter adapter = new OrderDetailAdapter(foodList);
        itemsRecyclerView.setAdapter(adapter);
    }

    private void setupEventListeners() {
        backBtn.setOnClickListener(v -> finish());
        contactBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:19001234"));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(TrackOrderActivity.this, "Không thể mở ứng dụng gọi điện.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}