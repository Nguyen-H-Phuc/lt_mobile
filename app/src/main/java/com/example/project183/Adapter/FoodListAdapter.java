package com.example.project183.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.project183.Activity.DetailActivity;
import com.example.project183.Domain.Foods;
import com.example.project183.R;

import java.util.ArrayList;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.viewholder> {
    private ArrayList<Foods> items;
    private ArrayList<Foods> originalList; // Danh sách gốc
    private Context context;

    public FoodListAdapter(ArrayList<Foods> items) {
        this.items = new ArrayList<>(items);
        this.originalList = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public FoodListAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new viewholder(LayoutInflater.from(context).inflate(R.layout.viewholder_list_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodListAdapter.viewholder holder, int position) {
        Foods food = items.get(position);

        holder.titleTxt.setText(food.getTitle());
        holder.timeTxt.setText(food.getTimeValue() + " min");
        holder.priceTxt.setText("$" + food.getPrice());
        holder.rateTxt.setText("" + food.getStar());

        Glide.with(context)
                .load(food.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(50))
                .into(holder.pic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", food);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ✅ Hàm lọc tìm kiếm theo từ khóa
    public void filter(String keyword) {
        keyword = keyword.toLowerCase().trim();
        items.clear();

        if (keyword.isEmpty()) {
            items.addAll(originalList);
        } else {
            for (Foods item : originalList) {
                if (item.getTitle().toLowerCase().contains(keyword)) {
                    items.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    public class viewholder extends RecyclerView.ViewHolder {

        TextView titleTxt, priceTxt, rateTxt, timeTxt;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);

            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            rateTxt = itemView.findViewById(R.id.ratingTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.img);
        }
    }
}
