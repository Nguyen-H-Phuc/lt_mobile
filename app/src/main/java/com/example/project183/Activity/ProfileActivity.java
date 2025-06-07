package com.example.project183.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.project183.R;
import com.example.project183.Domain.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView, addressTextView;
    private Button logoutButton, editProfileButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userDbRef;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Nếu bạn muốn nút back ở đây
        }

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        addressTextView = findViewById(R.id.addressTextView);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUserForDB = mAuth.getCurrentUser();
        if (currentUserForDB != null) {
            userDbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserForDB.getUid());
        } else {
            // Xử lý trường hợp không có người dùng đăng nhập ngay từ đầu
            Toast.makeText(this, "No user signed in. Redirecting to login.", Toast.LENGTH_LONG).show();
            goToLoginActivity();
            return; // Ngăn không cho code phía dưới chạy nếu chưa đăng nhập
        }

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Profile updated, reloading data.");
                        loadUserProfile();
                    }
                }
        );

        loadUserProfile();

        logoutButton.setOnClickListener(v -> logoutUser());

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // userDbRef đã được khởi tạo trong onCreate, không cần kiểm tra currentUser nữa nếu đã qua được đó

        if (currentUser == null || userDbRef == null) { // Thêm kiểm tra userDbRef
            Log.e(TAG, "User or DB ref is null in loadUserProfile. This should not happen.");
            goToLoginActivity();
            return;
        }


        // Lấy thông tin từ FirebaseAuth
        String authName = currentUser.getDisplayName();
        String authEmail = currentUser.getEmail();
        String authPhotoUrl = (currentUser.getPhotoUrl() != null) ? currentUser.getPhotoUrl().toString() : null;

        nameTextView.setText((authName != null && !authName.isEmpty()) ? authName : "N/A");
        emailTextView.setText((authEmail != null && !authEmail.isEmpty()) ? authEmail : "N/A");

        if (authPhotoUrl != null) {
            Glide.with(this)
                    .load(authPhotoUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Lấy thông tin chi tiết hơn từ Realtime Database
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User userDomain = snapshot.getValue(User.class);
                    if (userDomain != null) {
                        // Ưu tiên tên từ DB nếu có
                        if (userDomain.getName() != null && !userDomain.getName().isEmpty()) {
                            nameTextView.setText(userDomain.getName());
                        }
                        phoneTextView.setText((userDomain.getPhoneNumber() != null && !userDomain.getPhoneNumber().isEmpty()) ? userDomain.getPhoneNumber() : "N/A");
                        addressTextView.setText((userDomain.getAddress() != null && !userDomain.getAddress().isEmpty()) ? userDomain.getAddress() : "N/A");

                        // Ưu tiên ảnh từ DB nếu có và khác với ảnh từ Auth
                        if (userDomain.getAvatarUrl() != null && !userDomain.getAvatarUrl().isEmpty()) {
                            if (authPhotoUrl == null || !authPhotoUrl.equals(userDomain.getAvatarUrl())) {
                                Glide.with(ProfileActivity.this)
                                        .load(userDomain.getAvatarUrl())
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .error(R.drawable.ic_profile_placeholder)
                                        .circleCrop()
                                        .into(profileImageView);
                            }
                        } else if (authPhotoUrl == null) { // Nếu DB cũng không có ảnh và Auth cũng không có
                            profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                } else {
                    Log.d(TAG, "No profile data found in Realtime Database for user: " + currentUser.getUid());
                    phoneTextView.setText("N/A");
                    addressTextView.setText("N/A");
                    // Nếu ảnh từ Auth là null, đảm bảo placeholder được hiển thị
                    if (authPhotoUrl == null) {
                        profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch profile data from Realtime Database.", error.toException());
                Toast.makeText(ProfileActivity.this, "Failed to load profile details.", Toast.LENGTH_SHORT).show();
                phoneTextView.setText("N/A");
                addressTextView.setText("N/A");
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //  @Override
    //  public boolean onSupportNavigateUp() { // Nếu bạn dùng nút back trên ActionBar cho Activity này
    //      onBackPressed();
    //      return true;
    //  }
}