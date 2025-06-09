// package com.example.project183.Adapter;
package com.example.project183.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project183.Activity.TrackOrderActivity;
import com.example.project183.Domain.Order;
import com.example.project183.R;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// --- SỬA LỖI 1: Thay đổi chữ ký của lớp để sử dụng ViewHolder một cách chính xác ---
public class OrderAdapter2 extends RecyclerView.Adapter<OrderAdapter2.ViewHolder> {
    private ArrayList<Order> orderList;
    private Context context;

    public OrderAdapter2(ArrayList<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override // Thêm @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // --- SỬA LỖI 2: Đảm bảo layout name là chính xác. Giả sử là "viewholder_order" ---
        // Nếu tên file XML của bạn là viewholder_order2.xml, hãy đổi ở đây.
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_order2, parent, false);
        return new ViewHolder(view);
    }

    // --- SỬA LỖI 3: Xóa phương thức onBindViewHolder rỗng và sử dụng đúng phương thức được override ---
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Sử dụng Locale của Mỹ (US) để định dạng tiền tệ theo USD ($)
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = new Date(order.getTimestamp());

        holder.orderIdTxt.setText("Mã đơn: #" + order.getOrderId());
        holder.orderDateTxt.setText("Ngày: " + dateFormat.format(date));
        holder.orderTotalTxt.setText(currencyFormatter.format(order.getTotalPrice()));
        holder.orderStatusTxt.setText(order.getStatus());

        holder.detailsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, TrackOrderActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            context.startActivity(intent);
        });
    }

    @Override // Thêm @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTxt, orderDateTxt, orderTotalTxt, orderStatusTxt, detailsBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTxt = itemView.findViewById(R.id.orderIdTxt);
            orderDateTxt = itemView.findViewById(R.id.orderDateTxt);
            orderTotalTxt = itemView.findViewById(R.id.orderTotalTxt);
            orderStatusTxt = itemView.findViewById(R.id.orderStatusTxt);
            detailsBtn = itemView.findViewById(R.id.detailsBtn);
        }
    }
}