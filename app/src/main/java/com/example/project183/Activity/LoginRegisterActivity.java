package com.example.project183.Activity; // <-- THAY THẾ BẰNG PACKAGE CỦA BẠN

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.project183.R; // <-- THAY THẾ BẰNG PACKAGE CỦA BẠN

public class LoginRegisterActivity extends AppCompatActivity {

    // Khai báo các biến cho View
    private Button loginBtn;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        // Ánh xạ các View từ file layout XML
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        // Thiết lập sự kiện click cho nút "Đăng Ký"
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi nhấn, tạo một Intent để mở RegisterActivity
                Intent intent = new Intent(LoginRegisterActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Thiết lập sự kiện click cho nút "Đăng Nhập"
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Tạo LoginActivity và điều hướng đến đó.
                // Hiện tại, chúng ta sẽ tạm thời hiển thị một thông báo.
                Toast.makeText(LoginRegisterActivity.this, "Mở màn hình đăng nhập...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}