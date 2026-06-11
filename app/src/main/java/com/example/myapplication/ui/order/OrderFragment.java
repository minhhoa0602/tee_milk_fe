package com.example.myapplication.ui.order;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderItem;
import com.example.myapplication.model.ReviewRequest;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private EditText etSearchOrder;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvOrders;
    private TextView tvEmptyOrder;

    private OrderAdapter orderAdapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        etSearchOrder = view.findViewById(R.id.etSearchOrder);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmptyOrder = view.findViewById(R.id.tvEmptyOrder);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(filteredOrders, this);
        rvOrders.setAdapter(orderAdapter);

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        etSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadOrders();
    }

    private void loadOrders() {
        apiService.getOrders("ALL").enqueue(new Callback<BaseResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Order>>> call, Response<BaseResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body().getData();
                    if (allOrders == null) allOrders = new ArrayList<>();
                    applyFilters();
                } else if (response.code() == 401) {
                    Toast.makeText(getContext(), "Phiên làm việc hết hạn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Order>>> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = etSearchOrder.getText().toString().toLowerCase(Locale.ROOT).trim();
        int checkedChipId = chipGroupFilter.getCheckedChipId();

        filteredOrders = allOrders.stream().filter(order -> {
            // Status filter
            boolean matchesStatus = true;
            if (checkedChipId == R.id.chipPending) {
                matchesStatus = "PENDING".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus()) || "SHIPPING".equals(order.getStatus());
            } else if (checkedChipId == R.id.chipCompleted) {
                matchesStatus = "COMPLETED".equals(order.getStatus());
            } else if (checkedChipId == R.id.chipCancelled) {
                matchesStatus = "CANCELLED".equals(order.getStatus());
            }

            // Search filter
            boolean matchesSearch = query.isEmpty() ||
                    String.valueOf(order.getId()).contains(query) ||
                    (order.getStatus() != null && order.getStatus().toLowerCase().contains(query)) ||
                    (order.getOrderItems() != null && order.getOrderItems().stream().anyMatch(item -> item.getProductName().toLowerCase().contains(query)));

            return matchesStatus && matchesSearch;
        }).collect(Collectors.toList());

        orderAdapter.setOrderList(filteredOrders);

        if (filteredOrders.isEmpty()) {
            tvEmptyOrder.setVisibility(View.VISIBLE);
        } else {
            tvEmptyOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReorderClick(Order order) {
        apiService.reorder(order.getId()).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã thêm lại đơn hàng vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi đặt lại đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReviewClick(Order order, OrderItem item) {
        showReviewDialog(order.getId(), item);
    }

    private void showReviewDialog(int orderId, OrderItem item) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        TextView tvProductName = dialogView.findViewById(R.id.tvReviewProductName);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);
        android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        tvProductName.setText(item.getProductName());

        new AlertDialog.Builder(requireContext())
                .setTitle("Đánh giá sản phẩm")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    int rating = (int) ratingBar.getRating();
                    String comment = etComment.getText().toString();
                    postReview(new ReviewRequest(orderId, item.getProductId(), rating, comment, null));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void postReview(ReviewRequest request) {
        apiService.postReview(request).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 400 || response.code() == 409 || response.code() == 500) {
                    // Check for duplicate or other errors
                    Toast.makeText(getContext(), "Bạn đã đánh giá sản phẩm này rồi.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
