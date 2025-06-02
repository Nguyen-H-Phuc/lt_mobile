package com.example.project183.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project183.Domain.Foods;
import com.example.project183.Helper.ManagementOrder;
import com.example.project183.Helper.ManagmentCart;
import com.example.project183.Helper.MapboxHelper;
import com.example.project183.R;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {
    private EditText editName, editPhone, editAddress;
    private TextView totalPriceText;
    private Button buttonPlaceOrder;
    private ManagmentCart managmentCart;
    private ManagementOrder managementOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

//        editName = findViewById(R.editName);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        totalPriceText = findViewById(R.id.totalPriceText);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);

        managmentCart = new ManagmentCart(this);
        double total = managmentCart.getTotalFee();
        totalPriceText.setText("Tổng tiền: " + total + "đ");
        buttonPlaceOrder.setOnClickListener(v -> placeOrder(total));
    }

    private void placeOrder(double total){
        managementOrder = new ManagementOrder();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        if( phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        MapboxHelper.geocodeAddress(this, address, new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().features().isEmpty()) {
                    Point origin = response.body().features().get(0).center();
                    Point destination = Point.fromLngLat(106.7000, 10.7769);

                    MapboxHelper.calculateDistance(getApplicationContext(), origin, destination, new MapboxHelper.DistanceCallback() {
                        @Override
                        public void onSuccess(double distanceInKm) {
                            Toast.makeText(getApplicationContext(), "Khoảng cách: " + distanceInKm + " km", Toast.LENGTH_LONG).show();
                            proceedWithOrder(distanceInKm, address);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(getApplicationContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Không tìm được địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thêm tham số địa chỉ để gửi đơn
    private void proceedWithOrder(double distanceInKm, String address) {
        double shippingFee = distanceInKm * 5000; // 5000đ mỗi km
        double total = managmentCart.getTotalFee() + shippingFee;
        totalPriceText.setText("Tổng tiền (đã cộng phí vận chuyển): " + total + "đ");

        ArrayList<Foods> cartList = managmentCart.getListCart();

        if (cartList != null && !cartList.isEmpty()) {
            managementOrder.sendOrderToFireBase("user123", address, cartList, total, 100);
        }

        Toast.makeText(this, "Đơn hàng đã được đặt!", Toast.LENGTH_LONG).show();
        finish();
    }

}
