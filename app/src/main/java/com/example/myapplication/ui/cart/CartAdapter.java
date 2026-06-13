package com.example.myapplication.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.cart.CartItem;


import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;
    private CartItemListener listener;

    // Interface để gửi sự kiện ngược về Fragment xử lý
    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQty);
        void onItemDeleted(int cartItemId, int position);
        void onItemCheckChanged();
        void onItemEditRequested(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, CartItemListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        // 1. Đổ dữ liệu chữ cơ bản
        holder.tvName.setText(item.getProductName());
        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(formatMoney(item.getTotalPrice()));

        // Dịch mức đá/đường hiển thị lên UI cho đẹp
        String iceText = item.getIceLevel().equals("NONE") ? "Không đá" : (item.getIceLevel().equals("LESS") ? "Ít đá" : "Đá thường");
        String sugarText = item.getSugarLevel().equals("NONE") ? "0% đường" : (item.getSugarLevel().equals("LESS") ? "50% đường" : "100% đường");
        holder.tvDetails.setText("Size " + item.getProductSize() + " | " + iceText + " | " + sugarText);

        // 2. Nối mảng Topping thành chuỗi cách nhau bởi dấu phẩy
        if (item.getToppingNames() != null && !item.getToppingNames().isEmpty()) {
            StringBuilder toppings = new StringBuilder();
            for (int i = 0; i < item.getToppingNames().size(); i++) {
                toppings.append(item.getToppingNames().get(i));
                if (i < item.getToppingNames().size() - 1) toppings.append(", ");
            }
            holder.tvToppings.setText("Topping: " + toppings.toString());
            holder.tvToppings.setVisibility(View.VISIBLE);
        } else {
            holder.tvToppings.setVisibility(View.GONE);
        }

        // 3. Load ảnh bằng Glide
        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivProduct);

        // 4. Quản lý trạng thái ô Checkbox (Tích chọn món)
        holder.cbItem.setOnCheckedChangeListener(null); // Reset tránh lỗi loop view
        holder.cbItem.setChecked(item.isChecked());
        holder.cbItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            listener.onItemCheckChanged(); // Báo Fragment tính lại tổng tiền
        });

        // 5. Sự kiện bấm nút (+) và (-)
        holder.btnPlus.setOnClickListener(v -> listener.onQuantityChanged(item, item.getQuantity() + 1));
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(item, item.getQuantity() - 1);
            }
        });

        // 6. Sự kiện bấm nút Xóa món
        holder.btnDelete.setOnClickListener(v -> listener.onItemDeleted(item.getCartItemId(), position));

        holder.btnEdit.setOnClickListener(v -> listener.onItemEditRequested(item)); // Bấm là báo về Fragment
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    private String formatMoney(java.math.BigDecimal amount) {
        return new DecimalFormat("#,###đ").format(amount);
    }

    // Lớp giữ ánh xạ các ID giao diện
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbItem;
        ImageView ivProduct;
        TextView tvName, tvDetails, tvToppings, tvQty, tvPrice, btnMinus, btnPlus, btnDelete, btnEdit;

        public CartViewHolder(@NonNull View v) {
            super(v);
            cbItem = v.findViewById(R.id.cbItem);
            ivProduct = v.findViewById(R.id.ivProduct);
            tvName = v.findViewById(R.id.tvName);
            tvDetails = v.findViewById(R.id.tvDetails);
            tvToppings = v.findViewById(R.id.tvToppings);
            tvQty = v.findViewById(R.id.tvQty);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }
}
