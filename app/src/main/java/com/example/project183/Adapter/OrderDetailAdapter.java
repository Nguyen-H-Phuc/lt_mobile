package com.example.project183.Adapter; // Đảm bảo đúng package

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Bạn cần thêm thư viện Glide
import com.example.project183.Domain.Foods;
import com.example.project183.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private List<Foods> items;
    private Context context;

    public OrderDetailAdapter(List<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_order_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Foods food = items.get(position);

        holder.titleTxt.setText(food.getTitle());

        // --- SỬA Ở ĐÂY ---
        // Gọi phương thức getQuantity() thay vì getNumberInCart()
        holder.quantityTxt.setText("Số lượng: " + food.getQuantity());

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        // Tính toán lại giá tiền dựa trên getQuantity()
        double totalItemPrice = food.getQuantity() * food.getPrice();
        holder.totalEachItemTxt.setText(currencyFormatter.format(totalItemPrice));

        // Tải ảnh bằng Glide
        // Giả sử bạn có phương thức getImagePath() trong class Foods
        Glide.with(context)
                .load(food.getImagePath())
                .into(holder.itemPic);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemPic;
        TextView titleTxt, quantityTxt, totalEachItemTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemPic = itemView.findViewById(R.id.itemPic);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            quantityTxt = itemView.findViewById(R.id.quantityTxt);
            totalEachItemTxt = itemView.findViewById(R.id.totalEachItemTxt);
        }
    }
}