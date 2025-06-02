package com.example.project183.service;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project183.Domain.User;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserAuthService {
    private FirebaseAuth mAuth;
    private final DatabaseReference userRef;

    public UserAuthService() {
        this.mAuth = FirebaseAuth.getInstance();
        this.userRef = FirebaseDatabase.getInstance().getReference("user");
    }

    public void startPhoneNumberVerification(Activity active, String phoneNumber,
                                             PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Số điện thoại cần xác minh
                        .setTimeout(60L, TimeUnit.SECONDS) // Hết hạn sau 60 giây
                        .setActivity(active) // Bắt buộc: activity hiện tại (gán từ bên ngoài)
                        .setCallbacks(callbacks)            // Xử lý callback
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Xác thực bằng mã OTP
    public void verifyCode(String verificationId, String code,
                           PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential, callbacks);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential,
                                               PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Auth", "Đăng nhập thành công");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToRealtimeDatabase(firebaseUser);
                        }
                        callbacks.onVerificationCompleted(credential);
                    } else {
                        Log.e("Auth", "Đăng nhập thất bại", task.getException());

                        if(task.getException() instanceof FirebaseException) {
                            callbacks.onVerificationFailed((FirebaseException) task.getException());
                        }
                    }
                });
    }

    private void saveUserToRealtimeDatabase(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DatabaseReference userNode = userRef.child(uid);
        userNode.get().addOnSuccessListener(snapshop -> {
            if (!snapshop.exists()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("uid", uid);
                userMap.put("phoneNumber", firebaseUser.getPhoneNumber());
                userMap.put("name", "");
                userMap.put("email", "");
                userMap.put("avatarUrl", "");
                userMap.put("address", "");
                userMap.put("role", "user");
                userMap.put("isActive", true);

                userNode.setValue(userMap);
            } else {
                userNode.child("lastLoginTime").setValue(System.currentTimeMillis());
            }

        });
    }

    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }

    public void getUserToRealtimeDatabase(UserCallback callback){
        String uid = getCurrentUser().getUid();
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    callback.onUserLoaded(user);
                } else {
                    callback.onUserLoaded(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserLoaded(null);
            }
        });
    }

}
