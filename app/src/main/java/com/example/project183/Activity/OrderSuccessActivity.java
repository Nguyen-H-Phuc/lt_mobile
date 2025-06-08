package com.example.project183.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project183.R;

public class OrderSuccessActivity extends AppCompatActivity {
    Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        btnBackHome = findViewById(R.id.btnBackHome);

        btnBackHome.setOnClickListener(view -> {
            // Chuyển về màn hình chính
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}