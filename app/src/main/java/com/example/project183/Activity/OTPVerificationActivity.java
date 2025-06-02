package com.example.project183.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project183.R;
import com.example.project183.service.UserAuthService;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPVerificationActivity extends AppCompatActivity {
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify = findViewById(R.id.btnVerify);

        setupOtpInputs();

        btnVerify.setOnClickListener(v -> {
            String code = otp1.getText().toString().trim() +
                    otp2.getText().toString().trim() +
                    otp3.getText().toString().trim() +
                    otp4.getText().toString().trim() +
                    otp5.getText().toString().trim() +
                    otp6.getText().toString().trim();

            if (code.length() == 6) {
                String verificationId = getIntent().getStringExtra("verificationId");
                new UserAuthService().verifyCode(verificationId, code, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }


                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OTPVerificationActivity.this, "Xác minh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupOtpInputs() {
        otp1.addTextChangedListener(new GenericTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new GenericTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new GenericTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new GenericTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new GenericTextWatcher(otp5, otp6));
    }

}
