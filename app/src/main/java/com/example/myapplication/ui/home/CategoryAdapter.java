package com.example.myapplication.ui.home;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<CategoryResponse> categories = new ArrayList<>();
    private int selectedPosition = 0;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryResponse category, int position);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<CategoryResponse> list) {
        categories.clear();

        // Thêm tab "Tất cả" vào đầu (id = null)
        CategoryResponse allCategory = new CategoryResponse();
        allCategory.setId(null);
        allCategory.setName("Tat ca");
        categories.add(allCategory);

        categories.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_tab, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryResponse category = categories.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvCategoryName.setText(category.getName());

        if (position == selectedPosition) {
            holder.flBackground.setBackgroundResource(R.drawable.bg_category_selected);
            holder.tvCategoryName.setTextColor(
                    ContextCompat.getColor(ctx, R.color.milktea_primary));
            holder.tvCategoryName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.flBackground.setBackgroundResource(R.drawable.bg_category_normal);
            holder.tvCategoryName.setTextColor(
                    ContextCompat.getColor(ctx, R.color.milktea_text_secondary));
            holder.tvCategoryName.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onCategoryClick(category, selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // public để HomeFragment truy cập được
    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flBackground;
        TextView tvCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            flBackground   = itemView.findViewById(R.id.flCategoryBg);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}