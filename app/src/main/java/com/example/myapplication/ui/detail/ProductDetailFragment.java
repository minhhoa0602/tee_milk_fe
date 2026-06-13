package com.example.myapplication.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.api.ProductApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.ProductDetailResponse;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProductDetailFragment
 *
 * Nhận productId qua Bundle, gọi:
 *   GET /api/products/{id}          → hiển thị chi tiết
 *   GET /api/products/{id}/options  → TODO: bottom sheet chọn size/topping
 */
public class ProductDetailFragment extends Fragment {

    public static final String ARG_PRODUCT_ID = "productId";

    private ProductApiService productApiService;
    private ReviewAdapter reviewAdapter;
    private int productId;

    public static ProductDetailFragment newInstance(int productId) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productApiService = RetrofitClient.getInstance(requireContext())
                .create(ProductApiService.class);

        // Lấy productId từ Bundle
        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }

        // Setup review adapter
        reviewAdapter = new ReviewAdapter();
        RecyclerView rvReviews = view.findViewById(R.id.rvReviews);
        rvReviews.setAdapter(reviewAdapter);

        // Nút back
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Nút "Chon mon" → TODO: gọi GET /api/products/{id}/options
        view.findViewById(R.id.btnChooseItem).setOnClickListener(v ->
                loadProductOptions());

        // Load chi tiết sản phẩm
        loadProductDetail(view);
    }

    // ================================================================
    // GET /api/products/{id}
    // ================================================================
    private void loadProductDetail(View view) {
        productApiService.getProductDetail(productId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<ProductDetailResponse>> call,
                                   @NonNull Response<BaseResponse<ProductDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    bindData(view, response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<ProductDetailResponse>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), "Khong tai duoc chi tiet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================================================================
    // BIND DATA → UI
    // ================================================================
    private void bindData(View view, ProductDetailResponse product) {

        // Ảnh
        ImageView ivImage = view.findViewById(R.id.ivProductImage);
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.color.milktea_bg)
                .centerCrop()
                .into(ivImage);

        // Tên
        ((TextView) view.findViewById(R.id.tvProductName))
                .setText(product.getName());

        // Giá
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ((TextView) view.findViewById(R.id.tvProductPrice))
                .setText(fmt.format(product.getBasePrice()) + "d");

        // Rating
        String ratingText = product.getAverageRating() != null
                ? String.format(Locale.US, "%.1f", product.getAverageRating())
                : "0.0";
        ((TextView) view.findViewById(R.id.tvRating)).setText(ratingText);
        ((TextView) view.findViewById(R.id.tvAvgRating)).setText(ratingText);

        // Sold count
        String soldText = product.getSoldCount() != null
                ? "(" + product.getSoldCount() + " da ban)"
                : "";
        ((TextView) view.findViewById(R.id.tvSoldCount)).setText(soldText);

        // Mô tả
        ((TextView) view.findViewById(R.id.tvDescription))
                .setText(product.getDescription());

        // Reviews
        TextView tvNoReview = view.findViewById(R.id.tvNoReview);
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            reviewAdapter.setReviews(product.getReviews());
            tvNoReview.setVisibility(View.GONE);
        } else {
            tvNoReview.setVisibility(View.VISIBLE);
        }
    }

    // ================================================================
    // GET /api/products/{id}/options → TODO: Bottom Sheet
    // ================================================================
    private void loadProductOptions() {
        productApiService.getProductOptions(productId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<Object>> call,
                                   @NonNull Response<BaseResponse<Object>> response) {
                if (response.isSuccessful()) {
                    // TODO: Mở BottomSheetDialogFragment chọn Size, Topping, Duong, Da
                    Toast.makeText(getContext(), "Mo chon mon...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<Object>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), "Khong tai duoc tuy chon", Toast.LENGTH_SHORT).show();
            }
        });
    }
}