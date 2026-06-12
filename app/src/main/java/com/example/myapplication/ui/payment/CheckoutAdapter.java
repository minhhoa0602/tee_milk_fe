package com.example.myapplication.ui.payment;
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
import com.example.myapplication.model.cart.CartItem;

import java.text.DecimalFormat;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {
    private Context context;
    private List<CartItem> itemList;

    public CheckoutAdapter(Context context, List<CartItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvQty.setText("x" + item.getQuantity());
        holder.tvPrice.setText(new DecimalFormat("#,###đ").format(item.getTotalPrice()));

        String iceText = item.getIceLevel().equals("NONE") ? "Không đá" : (item.getIceLevel().equals("LESS") ? "Ít đá" : "Đá thường");
        String sugarText = item.getSugarLevel().equals("NONE") ? "0% đường" : (item.getSugarLevel().equals("LESS") ? "50% đường" : "100% đường");
        holder.tvDetails.setText("Size " + item.getProductSize() + " | " + iceText + " | " + sugarText);

        if (item.getToppingNames() != null && !item.getToppingNames().isEmpty()) {
            holder.tvToppings.setText("Topping: " + String.join(", ", item.getToppingNames()));
            holder.tvToppings.setVisibility(View.VISIBLE);
        } else {
            holder.tvToppings.setVisibility(View.GONE);
        }

        Glide.with(context).load(item.getProductImage()).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivProduct);
    }

    @Override
    public int getItemCount() { return itemList != null ? itemList.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvQty, tvDetails, tvToppings, tvPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvToppings = itemView.findViewById(R.id.tvToppings);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
