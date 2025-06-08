package com.example.project183.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project183.Adapter.OrderAdapter;
import com.example.project183.Domain.Foods;
import com.example.project183.Domain.OrderFood;
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
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;

    private TextView edtPriceListFood, addressLabel, edtAddress, edtDeliveryFee, edtDeliveryTime, edtTotalPrice;
    private Button buttonPlaceOrder;
    private ManagmentCart managmentCart;
    private ManagementOrder managementOrder;
    private ActivityResultLauncher<Intent> mapActivityLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        managmentCart = new ManagmentCart(this);
        managementOrder = new ManagementOrder();
        recyclerView = findViewById(R.id.list_item);
        ArrayList<Foods> cartList = managmentCart.getListCart();
        List<OrderFood> orderFoods = new ArrayList<>();
        for (Foods food : cartList) {
            OrderFood orderFood = new OrderFood(
                    food.getId(),
                    food.getTitle(),
                    food.getPrice(),
                    food.getNumberInCart(),
                    food.getImagePath()
            );
            orderFoods.add(orderFood);
        }
        Log.d("MainActivity", "orderFoods size = " + orderFoods.size());
        for (OrderFood food : orderFoods) {
            Log.d("MainActivity", "Food: " + food.getTitle() + ", qty: " + food.getQuantity());
        }

        adapter = new OrderAdapter(orderFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        edtPriceListFood = findViewById(R.id.priceListFood);
        addressLabel = findViewById(R.id.addressLabel);
        edtAddress = findViewById(R.id.address);
        edtDeliveryFee = findViewById(R.id.deliveryFee);
        edtDeliveryTime = findViewById(R.id.deliveryTime);
        edtTotalPrice = findViewById(R.id.totalPrice);
        buttonPlaceOrder = findViewById(R.id.checkOutBtn);


        double total = managmentCart.getTotalFee();
        edtPriceListFood.setText(total + " $");
        mapActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String address = result.getData().getStringExtra("selected_address");
                    if (address != null) {
                        // Xử lý hiển thị địa chỉ
                        edtAddress.setText(address);
                        MapboxHelper.geocodeAddress(this,address, new Callback<GeocodingResponse>() {
                            @Override
                            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                                Point origin = response.body().features().get(0).center();
                                MapboxHelper.getRoute(CheckoutActivity.this, origin, new MapboxHelper.RouteResultCallback() {
                                    @Override
                                    public void onRouteFound(int durationInMinutes, int distanceInKm) {
                                        double deliveryFee = distanceInKm * 0.5;
                                        edtDeliveryFee.setText(deliveryFee + " $");
                                        edtDeliveryTime.setText(durationInMinutes + " minutes");
                                        edtTotalPrice.setText((managmentCart.getTotalFee() + deliveryFee)+ " $");
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(CheckoutActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                            @Override
                            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }
        );

        addressLabel.setOnClickListener(view -> {
            Intent intent = new Intent(CheckoutActivity.this, MapActivity.class);
            mapActivityLauncher.launch(intent);
        });
        buttonPlaceOrder.setOnClickListener(view -> {
            proceedWithOrder(orderFoods);
        });

    }

    private void proceedWithOrder(List<OrderFood> orderFoods) {
        String address = edtAddress.getText().toString().trim();

        String priceRaw = edtPriceListFood.getText().toString().trim();
        String feeRaw = edtDeliveryFee.getText().toString().trim();
        String timeRaw = edtDeliveryTime.getText().toString().trim();
        String totalRaw = edtTotalPrice.getText().toString().trim();

        if (address.isEmpty() || priceRaw.isEmpty() || feeRaw.isEmpty() || timeRaw.isEmpty() || totalRaw.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin đơn hàng!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String priceClean = priceRaw.replaceAll("[ $]", "").replace(",", ".");
            double priceListFood = Double.parseDouble(priceClean);

            String feeClean = feeRaw.replaceAll("[ $]", "").replace(",", ".");
            double deliveryFee = Double.parseDouble(feeClean);

            String timeClean = timeRaw.replaceAll(" minutes", "").trim();
            int deliveryTime = Integer.parseInt(timeClean);

            String totalClean = totalRaw.replaceAll("[ $]", "").replace(",", ".");
            double totalPrice = Double.parseDouble(totalClean);

            managementOrder.sendOrderToFireBase(this, address, orderFoods, priceListFood, deliveryFee, deliveryTime, totalPrice);
        } catch (NumberFormatException e) {
            Log.e("Checkout", "Lỗi parse số: " + e.getMessage());
            Toast.makeText(this, "Giá trị số không hợp lệ! " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("Checkout", "Lỗi không xác định: ", e);
            Toast.makeText(this, "Có lỗi xảy ra khi xử lý đơn hàng.", Toast.LENGTH_LONG).show();
        }

    }

}
