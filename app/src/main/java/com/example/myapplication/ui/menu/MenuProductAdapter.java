package com.example.myapplication.ui.menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.ProductResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MenuProductAdapter extends RecyclerView.Adapter<MenuProductAdapter.VH> {

    public interface Listener {
        void onProductClick(int productId);
        void onAddClick(int productId);
    }

    private List<ProductResponse> items = new ArrayList<>();
    private final Listener listener;

    public MenuProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ProductResponse> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProductResponse p = items.get(position);

        h.tvName.setText(p.getName());
        h.tvPrice.setText(formatMoney(p.getBasePrice()));

        // Rating + reviewCount
        if (p.getAverageRating() != null && p.getAverageRating() > 0) {
            String ratingText = String.format("%.1f", p.getAverageRating());
            if (p.getReviewCount() != null) ratingText += " (" + p.getReviewCount() + ")";
            h.tvRating.setText(ratingText);
            h.tvRating.setVisibility(View.VISIBLE);
            h.tvDot.setVisibility(View.VISIBLE);
        } else {
            h.tvRating.setVisibility(View.GONE);
            h.tvDot.setVisibility(View.GONE);
        }

        // Sold count
        int sold = p.getSoldCount() != null ? p.getSoldCount() : 0;
        h.tvSoldCount.setText("Đã bán " + sold);

        // Image — bo tròn góc 10dp theo bg_product_image
        Glide.with(h.ivImage.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.bg_product_image)
                .error(R.drawable.bg_product_image)
                .centerCrop()
                .transform(new com.bumptech.glide.load.resource.bitmap.RoundedCorners(
                        (int) (10 * h.ivImage.getContext().getResources().getDisplayMetrics().density)))
                .into(h.ivImage);

        h.itemView.setOnClickListener(v -> {
            if (p.getId() != null) listener.onProductClick(p.getId());
        });
        h.ivAdd.setOnClickListener(v -> {
            if (p.getId() != null) listener.onAddClick(p.getId());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage, ivAdd;
        TextView tvName, tvPrice, tvRating, tvDot, tvSoldCount;

        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProductImage);
            ivAdd = itemView.findViewById(R.id.ivAddToCart);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDot = itemView.findViewById(R.id.tvDot);
            tvSoldCount = itemView.findViewById(R.id.tvSoldCount);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0đ";
        return new DecimalFormat("#,###").format(amount.longValue()) + "đ";
    }
}
