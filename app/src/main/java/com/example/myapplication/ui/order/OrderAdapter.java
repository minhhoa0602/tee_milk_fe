package com.example.myapplication.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderItem;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onReorderClick(Order order);
        void onReviewClick(Order order, OrderItem item);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Mã đơn: #" + order.getId());
        holder.tvOrderDate.setText("Ngày đặt: " + order.getCreatedAt());
        holder.tvOrderTotal.setText("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + "đ");
        holder.tvOrderStatus.setText(mapStatus(order.getStatus()));

        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                itemsBuilder.append("- ").append(item.getProductName())
                        .append(" x").append(item.getQuantity()).append("\n");
            }
        }
        holder.tvOrderItems.setText(itemsBuilder.toString().trim());

        holder.btnReorder.setOnClickListener(v -> listener.onReorderClick(order));

        if ("COMPLETED".equals(order.getStatus())) {
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.btnReview.setOnClickListener(v -> {
                if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    listener.onReviewClick(order, order.getOrderItems().get(0));
                }
            });
        } else {
            holder.btnReview.setVisibility(View.GONE);
        }
    }

    private String mapStatus(String status) {
        if (status == null) return "N/A";
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "PROCESSING": return "Đang xử lý";
            case "SHIPPING": return "Đang giao";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderItems, tvOrderTotal;
        Button btnReorder, btnReview;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnReorder = itemView.findViewById(R.id.btnReorder);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}
