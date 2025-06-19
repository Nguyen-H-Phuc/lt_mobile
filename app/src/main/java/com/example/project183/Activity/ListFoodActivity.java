package com.example.project183.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.project183.Adapter.FoodListAdapter;
import com.example.project183.Domain.Foods;
import com.example.project183.R;
import com.example.project183.databinding.ActivityListFoodBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFoodActivity extends BaseActivity {
    ActivityListFoodBinding binding;
    private int categoryId;

    private ArrayList<Foods> list; // Danh sách gốc
    private FoodListAdapter adapter; // Adapter có filter

    private String titleSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);
        list = new ArrayList<>();

        // Trường hợp ưu tiên: có CategoryId
        if (categoryId != -1) {
            Query query = myRef.orderByChild("CategoryId").equalTo(categoryId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isFinishing() || isDestroyed()) return;

                    if (snapshot.exists()) {
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            list.add(issue.getValue(Foods.class));
                        }
                    }

                    setupAdapterAndSearch();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        } else if (titleSearch != null && !titleSearch.isEmpty()) {
            // Truy vấn toàn bộ, rồi lọc theo Title
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isFinishing() || isDestroyed()) return;

                    if (snapshot.exists()) {
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            Foods food = issue.getValue(Foods.class);
                            if (food != null && food.getTitle() != null &&
                                    food.getTitle().toLowerCase().contains(titleSearch.toLowerCase())) {
                                list.add(food);
                            }
                        }
                    }

                    setupAdapterAndSearch();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            // Trường hợp không có gì để lọc – có thể xử lý tuỳ mục đích
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
        // Tìm theo từng ký tự gõ
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }
        });

        // Tìm khi nhấn nút kính lúp (btnSearch)
        binding.btnSearch.setOnClickListener(v -> {
            String keyword = binding.edtSearch.getText().toString();
            if (adapter != null) {
                adapter.filter(keyword);
            }
        });
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", -1); // -1 để biết là không có
        String categoryName = getIntent().getStringExtra("CategoryName");
        titleSearch = getIntent().getStringExtra("Title");

        if (categoryName != null) {
            binding.titleTxt.setText(categoryName);
        } else if (titleSearch != null) {
            binding.titleTxt.setText(getString(R.string.list_food_title, titleSearch));
        }

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupAdapterAndSearch() {
        if (!list.isEmpty()) {
            adapter = new FoodListAdapter(list);
            binding.foodListView.setLayoutManager(new LinearLayoutManager(ListFoodActivity.this));
            binding.foodListView.setAdapter(adapter);
            setupSearch();
        }
        binding.progressBar.setVisibility(View.GONE);
    }
}
