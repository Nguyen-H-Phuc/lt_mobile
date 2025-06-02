package com.example.project183.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project183.R;
import com.example.project183.service.UserAuthService;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private EditText edtPhone;
    private String phoneNumber;
    private UserAuthService userAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPhone = findViewById(R.id.edtPhone);
        Button btnSendOTP = findViewById(R.id.btnSendOTP);
        userAuthService = new UserAuthService();

        btnSendOTP.setOnClickListener(v -> {
            phoneNumber = edtPhone.getText().toString().trim();
            if(phoneNumber.startsWith("0")){
                phoneNumber = phoneNumber.substring(1,9);
            }
            if (!phoneNumber.isEmpty()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Xác nhận số điện thoại")
                        .setMessage("Bạn có chắc muốn gửi mã OTP đến số +84" + phoneNumber + " không?")
                        .setPositiveButton("Đồng ý", (dialog, which) ->
                                userAuthService.startPhoneNumberVerification(LoginActivity.this,
                                        "+84" + phoneNumber,
                                        callbacks))
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull com.google.firebase.auth.PhoneAuthCredential credential) {
                    // Tự động xác minh (có thể dùng luôn credential)
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(LoginActivity.this, "Gửi OTP thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Log.d("OTP", "OTP sent. verificationId: " + verificationId);
                    Intent intent = new Intent(LoginActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                }
            };
}