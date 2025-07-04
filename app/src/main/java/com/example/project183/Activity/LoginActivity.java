package com.example.project183.Activity; // <-- THAY THẾ BẰNG PACKAGE CỦA BẠN

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project183.R; // <-- THAY THẾ BẰNG PACKAGE CỦA BẠN
import com.example.project183.service.UserAuthService; // <-- GIỮ NGUYÊN
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    // --- Views ---
    private EditText edtPhone;
    private Button btnSendOTP, googleSignInButton, facebookSignInButton;
    private ProgressBar progressBar;

    // --- Phone Auth ---
    private String phoneNumber;
    private UserAuthService userAuthService;

    // --- Social Login ---
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn sử dụng layout có chứa tất cả các nút
        setContentView(R.layout.activity_login); // Hoặc activity_intro.xml

        // --- Khởi tạo ---
        userAuthService = new UserAuthService();
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();

        // --- Ánh xạ Views ---
        edtPhone = findViewById(R.id.edtPhone);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        facebookSignInButton = findViewById(R.id.facebookSignInButton);
        progressBar = findViewById(R.id.progressBar);

        // --- Cấu hình Google Sign-In ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- Thiết lập sự kiện click ---
        setupClickListeners();
    }

    private void setupClickListeners() {
        // 1. Sự kiện cho đăng nhập bằng SĐT
        btnSendOTP.setOnClickListener(v -> handlePhoneLogin());

        // 2. Sự kiện cho đăng nhập Google
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // 3. Sự kiện cho đăng nhập Facebook
        facebookSignInButton.setOnClickListener(v -> signInWithFacebook());
    }

    // =========================================================================================
    // VÙNG LOGIC XỬ LÝ ĐĂNG NHẬP BẰNG SỐ ĐIỆN THOẠI (GIỮ NGUYÊN)
    // =========================================================================================

    private void handlePhoneLogin() {
        phoneNumber = edtPhone.getText().toString().trim();
        if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.substring(1);
        }
        if (!phoneNumber.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE); // Hiển thị progressbar
            userAuthService.startPhoneNumberVerification(LoginActivity.this,
                    "+84" + phoneNumber,
                    callbacks);
        } else {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
        }
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull com.google.firebase.auth.PhoneAuthCredential credential) {
                    // Hiếm khi xảy ra, nhưng nếu có thì tự động đăng nhập
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "onVerificationFailed", e);
                    Toast.makeText(LoginActivity.this, "Gửi OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onCodeSent:" + verificationId);
                    Intent intent = new Intent(LoginActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                }
            };


    // =========================================================================================
    // VÙNG LOGIC XỬ LÝ ĐĂNG NHẬP GOOGLE & FACEBOOK (THÊM VÀO)
    // =========================================================================================

    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithFacebook() {
        progressBar.setVisibility(View.VISIBLE);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Đăng nhập Facebook bị hủy.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Lỗi đăng nhập Facebook.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Chuyển kết quả cho Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Kết quả trả về từ Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Xác thực Google thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Xác thực Facebook thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Phương thức chung để điều hướng sau khi đăng nhập thành công bằng mạng xã hội
    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        if (user != null) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}