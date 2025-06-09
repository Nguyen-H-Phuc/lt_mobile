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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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

                // <<< BỎ ĐI: Không còn gán sự kiện mở Lịch sử đơn hàng ở đây nữa
                /*
                binding.imageView5.setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, MyOrdersActivity.class));
                });
                */

                // Gợi ý: Bạn có thể gán imageView5 để mở ProfileActivity cho nhất quán
                binding.imageView5.setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                });

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
        DatabaseReference myRef = database.getReference("Banners");
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
                binding.progressBarBanner.setVisibility(View.GONE);
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
        binding.viewpager2.setPageTransformer(compositePageTransformer);
    }

    private void setVariable() {
        // Đặt item "home" được chọn mặc định khi khởi động MainActivity
        binding.bottomMenu.setItemSelected(R.id.home, true);

        binding.bottomMenu.setOnItemSelectedListener(i -> {
            if (i == R.id.home) {
                // Đã ở trang chủ, không cần làm gì
                Log.d("Navigation", "Home selected");
            } else if (i == R.id.cart) {
                Log.d("Navigation", "Cart selected, starting CartActivity");
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            } else if (i == R.id.favorites) {
                // <<< CẬP NHẬT Ở ĐÂY: Mở MyOrdersActivity khi nhấn vào Favorites
                Log.d("Navigation", "Favorites selected, starting MyOrdersActivity");
                startActivity(new Intent(MainActivity.this, MyOrdersActivity.class));
            } else if (i == R.id.profile) {
                Log.d("Navigation", "Profile selected, starting ProfileActivity");
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }

    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Category category = issue.getValue(Category.class);
                        if (category != null) {
                            list.add(category);
                        }
                    }

                    if (!list.isEmpty()) {
                        binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
                        binding.categoryView.setAdapter(new CategoryAdapter(list));
                    }
                }
                binding.progressBarCategory.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseCategory", "Error loading categories: " + error.getMessage());
                binding.progressBarCategory.setVisibility(View.GONE);
            }
        });
    }
}