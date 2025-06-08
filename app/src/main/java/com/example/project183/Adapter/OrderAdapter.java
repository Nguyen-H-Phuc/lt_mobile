package com.example.project183.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project183.Domain.OrderFood;
import com.example.project183.R;

import androidx.annotation.NonNull;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private final List<OrderFood> orderFoods;

    public OrderAdapter(List<OrderFood> orderFoods) {
        this.orderFoods = orderFoods;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull OrderAdapter.OrderViewHolder holder, int position) {
        OrderFood food = orderFoods.get(position);
        Log.d("Orderfoodpostion", "Item count = " + food.getTitle());
        holder.tvFoodName.setText(food.getTitle());
        holder.tvQuantity.setText(String.valueOf(food.getQuantity()));
        holder.tvFoodPrice.setText(String.format("%.0f $", food.getPrice()* food.getQuantity()));
    }

    @Override
    public int getItemCount() {
        Log.d("OrderAdapter", "Item count = " + orderFoods.size());
        return orderFoods.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvQuantity, tvFoodPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
        }
    }
}
