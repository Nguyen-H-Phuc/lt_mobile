package com.example.project183.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.project183.Domain.User;
import com.example.project183.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private ImageView editProfileImageView;
    private Button changeImageButton, saveProfileButton;
    private TextInputEditText nameEditText, phoneEditText, addressEditText;
    private ProgressBar editProfileProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userDbRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseUser currentUser;
    private Uri imageUri;
    private String currentAvatarUrlFromDB;
    private User currentUserDomainData; // Để lưu trữ dữ liệu User hiện tại từ DB

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editProfileImageView = findViewById(R.id.editProfileImageView);
        changeImageButton = findViewById(R.id.changeImageButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        editProfileProgressBar = findViewById(R.id.editProfileProgressBar);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userDbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in. Cannot edit profile.", Toast.LENGTH_LONG).show();
            finish(); // Kết thúc activity nếu không có người dùng
            return;
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(this).load(imageUri).circleCrop().into(editProfileImageView);
                    }
                }
        );

        changeImageButton.setOnClickListener(v -> openImageChooser());
        saveProfileButton.setOnClickListener(v -> saveProfileChanges());

        loadCurrentProfileData();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadCurrentProfileData() {
        if (currentUser == null || userDbRef == null) {
            Toast.makeText(this, "User not logged in or database reference error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        // Lấy thông tin từ FirebaseAuth
        String authName = currentUser.getDisplayName();
        Uri authPhotoUri = currentUser.getPhotoUrl();

        nameEditText.setText(authName);
        if (authPhotoUri != null) {
            Glide.with(this)
                    .load(authPhotoUri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(editProfileImageView);
        } else {
            editProfileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Lấy thông tin chi tiết từ Realtime Database
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    currentUserDomainData = snapshot.getValue(User.class); // Lưu lại để dùng khi update
                    if (currentUserDomainData != null) {
                        if (currentUserDomainData.getName() != null && !currentUserDomainData.getName().isEmpty()) {
                            nameEditText.setText(currentUserDomainData.getName());
                        }
                        phoneEditText.setText(currentUserDomainData.getPhoneNumber());
                        addressEditText.setText(currentUserDomainData.getAddress());
                        currentAvatarUrlFromDB = currentUserDomainData.getAvatarUrl();

                        if (currentAvatarUrlFromDB != null && !currentAvatarUrlFromDB.isEmpty()) {
                            if(authPhotoUri == null || !authPhotoUri.toString().equals(currentAvatarUrlFromDB)) {
                                Glide.with(EditProfileActivity.this)
                                        .load(currentAvatarUrlFromDB)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .error(R.drawable.ic_profile_placeholder)
                                        .circleCrop()
                                        .into(editProfileImageView);
                            }
                        } else if (authPhotoUri == null){
                            editProfileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                } else {
                    Log.d(TAG, "No profile data found in Realtime DB for user: " + currentUser.getUid());
                    currentUserDomainData = new User(); // Khởi tạo nếu chưa có để tránh NPE khi lưu
                    currentUserDomainData.setUid(currentUser.getUid());
                    currentUserDomainData.setEmail(currentUser.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Log.e(TAG, "Failed to fetch profile data from Realtime DB.", error.toException());
                Toast.makeText(EditProfileActivity.this, "Failed to load profile details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileChanges() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name cannot be empty");
            nameEditText.requestFocus();
            return;
        }
        showLoading(true);

        if (imageUri != null) {
            uploadImageAndUpdateProfile(name, phone, address);
        } else {
            // Nếu không đổi ảnh, dùng ảnh hiện tại (từ Auth hoặc từ DB nếu Auth null)
            String existingImageUrl = (currentUser.getPhotoUrl() != null) ? currentUser.getPhotoUrl().toString() : currentAvatarUrlFromDB;
            updateProfileTextInfo(name, phone, address, existingImageUrl);
        }
    }

    private void uploadImageAndUpdateProfile(String name, String phone, String address) {
        String imageName = "avatars/" + currentUser.getUid() + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageReference.child(imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newImageUrl = uri.toString();
                    updateProfileTextInfo(name, phone, address, newImageUrl);
                }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Image upload failed", e);
                });
    }

    private void updateProfileTextInfo(String name, String phone, String address, String imageUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri((imageUrl != null) ? Uri.parse(imageUrl) : null)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile (Auth) updated.");
                        updateRealtimeDBProfile(name, phone, address, imageUrl);
                    } else {
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this, "Failed to update Firebase Auth profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Auth profile update failed", task.getException());
                    }
                });
    }

    private void updateRealtimeDBProfile(String name, String phone, String address, String imageUrl) {
        if (userDbRef == null) {
            showLoading(false);
            Toast.makeText(this, "Database reference error. Cannot update profile.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng lại currentUserDomainData nếu đã load được, hoặc tạo mới nếu chưa có
        User userToUpdate = (currentUserDomainData != null) ? currentUserDomainData : new User();

        // Luôn set UID và Email từ currentUser để đảm bảo tính nhất quán
        userToUpdate.setUid(currentUser.getUid());
        userToUpdate.setEmail(currentUser.getEmail());

        // Cập nhật các trường đã thay đổi
        userToUpdate.setName(name);
        userToUpdate.setPhoneNumber(phone);
        userToUpdate.setAddress(address);
        if (imageUrl != null) {
            userToUpdate.setAvatarUrl(imageUrl);
        } else if (userToUpdate.getAvatarUrl() == null) { // Nếu imageUrl là null và chưa có avatarUrl
            userToUpdate.setAvatarUrl(""); // Hoặc giữ nguyên giá trị cũ nếu có
        }
        // Giữ nguyên các trường không chỉnh sửa (isActive, role) nếu có trong currentUserDomainData
        // Nếu currentUserDomainData là mới, bạn có thể đặt giá trị mặc định cho chúng
        if(currentUserDomainData == null || currentUserDomainData.getRole() == null) { // Ví dụ
            userToUpdate.setRole("user");
            userToUpdate.setActive(true);
        }


        userDbRef.setValue(userToUpdate)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Realtime Database profile updated.");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Failed to update Realtime DB profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Realtime DB profile update failed", e);
                });
    }


    private void showLoading(boolean isLoading) {
        if (isLoading) {
            editProfileProgressBar.setVisibility(View.VISIBLE);
            saveProfileButton.setEnabled(false);
            changeImageButton.setEnabled(false);
        } else {
            editProfileProgressBar.setVisibility(View.GONE);
            saveProfileButton.setEnabled(true);
            changeImageButton.setEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}