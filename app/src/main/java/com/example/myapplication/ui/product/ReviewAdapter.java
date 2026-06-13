package com.example.myapplication.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.ReviewItem;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<ReviewItem> reviews = new ArrayList<>();

    public void setReviews(List<ReviewItem> list) {
        reviews = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem r = reviews.get(position);
        holder.tvName.setText(r.getReviewerName());
        holder.ratingBar.setRating(r.getRatingStar());
        holder.tvComment.setText(r.getComment() != null ? r.getComment() : "");
        // Format ngày nếu có (chỉ lấy phần date)
        String date = r.getCreatedAt();
        if (date != null && date.length() >= 10) date = date.substring(0, 10);
        holder.tvDate.setText(date != null ? date : "");
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvComment, tvDate;
        RatingBar ratingBar;

        ReviewViewHolder(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvReviewerName);
            ratingBar = v.findViewById(R.id.ratingBarReview);
            tvComment = v.findViewById(R.id.tvReviewComment);
            tvDate    = v.findViewById(R.id.tvReviewDate);
        }
    }
}
