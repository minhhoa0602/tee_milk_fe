package com.example.myapplication.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.ReviewResponse;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewResponse> reviews = new ArrayList<>();

    public void setReviews(List<ReviewResponse> list) {
        this.reviews = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewResponse review = reviews.get(position);

        // Avatar: lấy chữ cái đầu của tên
        String name = review.getUserName() != null ? review.getUserName() : "?";
        holder.tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
        holder.tvReviewerName.setText(name);

        // Ngày
        holder.tvReviewDate.setText(
                review.getCreatedAt() != null ? review.getCreatedAt().substring(0, 10) : "");

        // Comment
        holder.tvReviewComment.setText(review.getComment());

        // Sao — tô màu theo số sao
        ImageView[] stars = {
                holder.star1, holder.star2, holder.star3, holder.star4, holder.star5
        };
        int rating = review.getRatingStar() != null ? review.getRatingStar() : 0;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars[i].setImageResource(android.R.drawable.star_on);
                stars[i].setColorFilter(0xFFFFB800);
            } else {
                stars[i].setImageResource(android.R.drawable.star_off);
                stars[i].setColorFilter(0xFFDDDDDD);
            }
        }
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarLetter, tvReviewerName, tvReviewDate, tvReviewComment;
        ImageView star1, star2, star3, star4, star5;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter  = itemView.findViewById(R.id.tvAvatarLetter);
            tvReviewerName  = itemView.findViewById(R.id.tvReviewerName);
            tvReviewDate    = itemView.findViewById(R.id.tvReviewDate);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
}