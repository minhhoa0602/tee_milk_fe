package com.example.myapplication.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList = new ArrayList<>();
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onReorderClick(Order order);
        void onReviewClick(Order order, OrderItem item);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.listener = listener;
    }

    public void setOrderList(List<Order> newList) {
        final List<Order> safeNew = newList != null ? newList : new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            final List<Order> old = orderList;
            final List<Order> next = safeNew;

            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return next.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                String a = old.get(oldPos).getOrderCode();
                String b = next.get(newPos).getOrderCode();
                return a != null && a.equals(b);
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Order a = old.get(oldPos), b = next.get(newPos);
                return strEq(a.getOrderStatus(), b.getOrderStatus()) &&
                        a.getTotalAmount() == b.getTotalAmount();
            }

            private boolean strEq(String a, String b) {
                return a == null ? b == null : a.equals(b);
            }
        });
        orderList = safeNew;
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText(order.getOrderCode() != null ? order.getOrderCode() : "N/A");
        holder.tvOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "");
        holder.tvOrderTotal.setText(String.format("%,.0fđ", order.getTotalAmount()));
        holder.tvOrderStatus.setText(mapStatus(order.getOrderStatus()));
        holder.tvOrderStatus.setTextColor(getStatusColor(order.getOrderStatus()));

        StringBuilder sb = new StringBuilder();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                sb.append("• ").append(item.getProductName())
                        .append(" x").append(item.getQuantity())
                        .append(" — ").append(String.format("%,.0fđ", item.getUnitPrice()))
                        .append("\n");
            }
        }
        holder.tvOrderItems.setText(sb.toString().trim());

        // Tách listener ra ngoài để tránh tạo lambda thừa
        holder.btnReorder.setOnClickListener(v -> listener.onReorderClick(order));

        if ("COMPLETED".equals(order.getOrderStatus())) {
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.btnReview.setOnClickListener(v -> {
                List<OrderItem> items = order.getOrderItems();
                if (items != null && !items.isEmpty()) {
                    listener.onReviewClick(order, items.get(0));
                }
            });
        } else {
            holder.btnReview.setVisibility(View.GONE);
            holder.btnReview.setOnClickListener(null);
        }
    }

    private String mapStatus(String status) {
        if (status == null) return "N/A";
        switch (status) {
            case "PENDING":    return "Chờ xác nhận";
            case "PROCESSING": return "Đang xử lý";
            case "SHIPPING":   return "Đang giao";
            case "COMPLETED":  return "Hoàn thành";
            case "CANCELLED":  return "Đã hủy";
            default:           return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return 0xFF555555;
        switch (status) {
            case "COMPLETED":  return 0xFF2E7D32;
            case "CANCELLED":  return 0xFFD32F2F;
            case "SHIPPING":   return 0xFF1565C0;
            default:           return 0xFFF57C00;
        }
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    // Cho RecyclerView biết mỗi item là duy nhất → tránh flicker khi update
    @Override
    public long getItemId(int position) {
        String code = orderList.get(position).getOrderCode();
        return code != null ? code.hashCode() : position;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderItems, tvOrderTotal;
        Button btnReorder, btnReview;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId     = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate   = itemView.findViewById(R.id.tvOrderDate);
            tvOrderItems  = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal  = itemView.findViewById(R.id.tvOrderTotal);
            btnReorder    = itemView.findViewById(R.id.btnReorder);
            btnReview     = itemView.findViewById(R.id.btnReview);
        }
    }
}
