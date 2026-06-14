package com.example.myapplication.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
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
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.TokenManager;
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
    private TokenManager tokenManager;

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

        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        etSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
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
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body().getData();
                    if (allOrders == null) allOrders = new ArrayList<>();
                    applyFilters();
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    Toast.makeText(getContext(), "Lỗi tải đơn hàng (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Order>>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Không thể kết nối máy chủ. Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = etSearchOrder.getText().toString().toLowerCase(Locale.ROOT).trim();
        int checkedChipId = chipGroupFilter.getCheckedChipId();

        filteredOrders = allOrders.stream().filter(order -> {
            boolean matchesStatus = true;
            if (checkedChipId == R.id.chipPending) {
                String s = order.getOrderStatus();
                matchesStatus = "PENDING".equals(s) || "PROCESSING".equals(s) || "SHIPPING".equals(s);
            } else if (checkedChipId == R.id.chipCompleted) {
                matchesStatus = "COMPLETED".equals(order.getOrderStatus());
            } else if (checkedChipId == R.id.chipCancelled) {
                matchesStatus = "CANCELLED".equals(order.getOrderStatus());
            }

            boolean matchesSearch = query.isEmpty() ||
                    (order.getOrderCode() != null && order.getOrderCode().toLowerCase().contains(query)) ||
                    (order.getOrderStatus() != null && order.getOrderStatus().toLowerCase().contains(query)) ||
                    (order.getOrderItems() != null && order.getOrderItems().stream()
                            .anyMatch(item -> item.getProductName() != null && item.getProductName().toLowerCase().contains(query)));

            return matchesStatus && matchesSearch;
        }).collect(Collectors.toList());

        orderAdapter.setOrderList(filteredOrders);
        tvEmptyOrder.setVisibility(filteredOrders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onReorderClick(Order order) {
        int numericId = order.getNumericId();
        if (numericId == 0) {
            Toast.makeText(getContext(), "Không xác định được mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.reorder(numericId).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã thêm lại đơn hàng vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi đặt lại đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReviewClick(Order order, OrderItem item) {
        showReviewDialog(order.getNumericId(), item);
    }

    private void showReviewDialog(int orderId, OrderItem item) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        TextView tvProductName = dialogView.findViewById(R.id.tvReviewProductName);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        tvProductName.setText(item.getProductName());

        new AlertDialog.Builder(requireContext())
                .setTitle("Đánh giá sản phẩm")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    int rating = (int) ratingBar.getRating();
                    if (rating == 0) {
                        Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String comment = etComment.getText().toString().trim();
                    postReview(new ReviewRequest(orderId, item.getProductId(), rating, comment, null));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void postReview(ReviewRequest request) {
        apiService.postReview(request).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    // 400/409/500 đều có thể là đánh giá trùng
                    Toast.makeText(getContext(), "Bạn đã đánh giá sản phẩm này rồi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        if (getActivity() == null) return;
        tokenManager.clearToken();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
