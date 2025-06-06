package com.example.project183.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.project183.Adapter.FoodListAdapter;
import com.example.project183.Domain.Foods;
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
    private String categoryName;

    private ArrayList<Foods> list; // Danh sách gốc
    private FoodListAdapter adapter; // Adapter có filter

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

        Query query = myRef.orderByChild("CategoryId").equalTo(categoryId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Foods.class));
                    }

                    if (!list.isEmpty()) {
                        adapter = new FoodListAdapter(list);
                        binding.foodListView.setLayoutManager(new LinearLayoutManager(ListFoodActivity.this));
                        binding.foodListView.setAdapter(adapter);
                        setupSearch(); // Khởi động tìm kiếm
                    }

                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");

        binding.titleTxt.setText(categoryName);
        binding.backBtn.setOnClickListener(v -> finish());
    }
}
