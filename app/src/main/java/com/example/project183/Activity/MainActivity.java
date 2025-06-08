package com.example.project183.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.project183.Adapter.CategoryAdapter;
import com.example.project183.Adapter.SliderAdapter;
import com.example.project183.Domain.Category;
import com.example.project183.Domain.SliderItems;
import com.example.project183.R;
import com.example.project183.databinding.ActivityMainBinding;
import com.example.project183.service.UserAuthService;
// import com.example.project183.Domain.User; // Không cần import lại ở đây nếu BaseActivity đã có
import com.example.project183.R;
import com.example.project183.databinding.ActivityMainBinding;
import com.example.project183.service.UserAuthService;
// import com.example.project183.service.UserCallback; // Không thấy được sử dụng trực tiếp

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

// import com.ismaeldivita.chipnavigation.ChipNavigationBar; // Không cần import nếu binding đã có

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    private UserAuthService userAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userAuthService = new UserAuthService();
        // Lấy thông tin người dùng và hiển thị
        userAuthService.getUserToRealtimeDatabase(user -> {
            // kiểm tra thông tin người dùng
            if (user != null) {
                Log.d("DEBUG_USER", "User: " + user.getPhoneNumber());
                // set tên nguười dùng nếu người dùng có tên
                if (user.getName() != null && !user.getName().isEmpty()) {
                    binding.textView2.setText(user.getName());
                } // không tìm thấy tên thì set số điện thoại
                else if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                    binding.textView2.setText(user.getPhoneNumber());
                } // để trống nếu không tìm thấy tên và thuộc tính
                else {
                    binding.textView2.setText("");
                }
            }
            // Không tìm thấy người dùng thì để trống
            else {
                binding.textView2.setText("");
            }
        });

        initCategory();
        initBanner();
        setVariable(); // Gọi sau khi các view đã được khởi tạo
    }

    private void initBanner() {
        DatabaseReference myRef = database.getReference("Banners"); // `database` được kế thừa từ BaseActivity
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        items.add(issue.getValue(SliderItems.class));
                    }
                    banners(items);
                }
                binding.progressBarBanner.setVisibility(View.GONE); // Luôn ẩn ProgressBar sau khi hoàn tất
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseBanner", "Error loading banners: " + error.getMessage());
                binding.progressBarBanner.setVisibility(View.GONE);
            }
        });
    }

    private void banners(ArrayList<SliderItems> items) {
        binding.viewpager2.setAdapter(new SliderAdapter(items, binding.viewpager2));
        binding.viewpager2.setClipChildren(false);
        binding.viewpager2.setClipToPadding(false);
        binding.viewpager2.setOffscreenPageLimit(3);
        binding.viewpager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        // Nếu bạn muốn có hiệu ứng scale cho item ở giữa (tùy chọn)
        // compositePageTransformer.addTransformer((page, position) -> {
        //     float r = 1 - Math.abs(position);
        //     page.setScaleY(0.85f + r * 0.15f);
        // });
        binding.viewpager2.setPageTransformer(compositePageTransformer);
    }

    private void setVariable() {
        // Đặt item "home" được chọn mặc định khi khởi động MainActivity
        binding.bottomMenu.setItemSelected(R.id.home, true);

        binding.bottomMenu.setOnItemSelectedListener(i -> {
            // 'i' ở đây chính là ID của item được chọn
            if (i == R.id.home) {
                // Đã ở trang chủ, không cần làm gì hoặc refresh nếu cần
                Log.d("Navigation", "Home selected");
            } else if (i == R.id.cart) {
                Log.d("Navigation", "Cart selected, starting CartActivity");
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            } else if (i == R.id.favorites) { // Giả sử bạn có item favorites
                Log.d("Navigation", "Favorites selected");
                // startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
            } else if (i == R.id.profile) { // XỬ LÝ CHO PROFILE
                Log.d("Navigation", "Profile selected, starting ProfileActivity");
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
            // ChipNavigationBar không yêu cầu return true/false trong listener này
        });
    }

    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category"); // `database` được kế thừa từ BaseActivity
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Category category = issue.getValue(Category.class);
                        if (category != null) { // Kiểm tra null để tránh lỗi
                            list.add(category);
                        }
                    }

                    if (!list.isEmpty()) { // Kiểm tra list không rỗng
                        binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4)); // Thử 4 cột nếu item nhỏ
                        binding.categoryView.setAdapter(new CategoryAdapter(list));
                    }
                }
                binding.progressBarCategory.setVisibility(View.GONE); // Luôn ẩn ProgressBar
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseCategory", "Error loading categories: " + error.getMessage());
                binding.progressBarCategory.setVisibility(View.GONE);
            }
        });
    }
}