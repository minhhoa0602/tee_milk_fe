package com.example.myapplication.ui.home;

import android.content.Context;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ViewHolder> {

    // Kiểu hiển thị badge
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RANK = 1;   // Best Seller: badge số 1, 2, 3
    public static final int MODE_NEW = 2;    // Sản phẩm mới: badge "NEW"

    private final int mode;
    private List<ProductResponse> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onItemClick(ProductResponse product);
        void onAddToCart(ProductResponse product);
    }

    public ProductCardAdapter(int mode, OnProductClickListener listener) {
        this.mode = mode;
        this.listener = listener;
    }

    public void setProducts(List<ProductResponse> newList) {
        this.products = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductResponse product = products.get(position);
        Context ctx = holder.itemView.getContext();

        // ---- Tên sản phẩm (field: name) ----
        holder.tvProductName.setText(product.getName());

        // ---- Giá (field: basePrice) ----
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String priceText = fmt.format(product.getBasePrice()) + "đ";
        holder.tvProductPrice.setText(priceText);

        // ---- Ảnh (field: imageUrl) ----
        Glide.with(ctx)
                .load(product.getImageUrl())
                .placeholder(R.color.milktea_bg)
                .centerCrop()
                .into(holder.ivProductImage);

        // ---- Badge theo mode ----
        holder.tvRankBadge.setVisibility(View.GONE);
        holder.tvNewBadge.setVisibility(View.GONE);

        if (mode == MODE_RANK && position < 3) {
            holder.tvRankBadge.setVisibility(View.VISIBLE);
            holder.tvRankBadge.setText(String.valueOf(position + 1));
            // Màu badge: vàng=1, bạc=2, đồng=3
            int[] rankColors = {0xFFFFD700, 0xFFC0C0C0, 0xFFCD7F32};
            holder.tvRankBadge.getBackground().setTint(rankColors[position]);
        } else if (mode == MODE_NEW) {
            holder.tvNewBadge.setVisibility(View.VISIBLE);
        }

        // ---- Click listeners ----
        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
        holder.ivAddToCart.setOnClickListener(v -> listener.onAddToCart(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivAddToCart;
        TextView tvProductName, tvProductPrice, tvRankBadge, tvNewBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage  = itemView.findViewById(R.id.ivProductImage);
            ivAddToCart     = itemView.findViewById(R.id.ivAddToCart);
            tvProductName   = itemView.findViewById(R.id.tvProductName);
            tvProductPrice  = itemView.findViewById(R.id.tvProductPrice);
            tvRankBadge     = itemView.findViewById(R.id.tvRankBadge);
            tvNewBadge      = itemView.findViewById(R.id.tvNewBadge);
        }
    }
}