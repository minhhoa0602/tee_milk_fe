package com.example.myapplication.ui.detail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.api.ProductApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.ProductDetailResponse;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.ui.order.OptionsBottomSheet;
import com.example.myapplication.utils.TokenManager;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }

        reviewAdapter = new ReviewAdapter();
        RecyclerView rvReviews = view.findViewById(R.id.rvReviews);
        rvReviews.setAdapter(reviewAdapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btnChooseItem).setOnClickListener(v -> onChooseItemClicked());

        loadProductDetail(view);
    }

    private void onChooseItemClicked() {
        TokenManager tokenManager = new TokenManager(requireContext());
        if (tokenManager.getToken() == null) {
            // Save pending action and redirect to login
            SharedPreferences prefs = requireContext().getSharedPreferences("PendingCart", AppCompatActivity.MODE_PRIVATE);
            prefs.edit()
                    .putInt("sizeId", 1)
                    .putString("iceLevel", "NORMAL")
                    .putString("sugarLevel", "NORMAL")
                    .putInt("quantity", 1)
                    .apply();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.putExtra(LoginActivity.EXTRA_PENDING_PRODUCT_ID, productId);
            startActivity(intent);
        } else {
            OptionsBottomSheet bottomSheet = new OptionsBottomSheet(productId);
            bottomSheet.show(getChildFragmentManager(), "OptionsBottomSheet");
        }
    }

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
                Toast.makeText(getContext(), "Không tải được chi tiết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(View view, ProductDetailResponse product) {
        ImageView ivImage = view.findViewById(R.id.ivProductImage);
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.color.milktea_bg)
                .centerCrop()
                .into(ivImage);

        ((TextView) view.findViewById(R.id.tvProductName)).setText(product.getName());

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ((TextView) view.findViewById(R.id.tvProductPrice))
                .setText(fmt.format(product.getBasePrice()) + "đ");

        String ratingText = product.getAverageRating() != null
                ? String.format(Locale.US, "%.1f", product.getAverageRating()) : "0.0";
        ((TextView) view.findViewById(R.id.tvRating)).setText(ratingText);
        ((TextView) view.findViewById(R.id.tvAvgRating)).setText(ratingText);

        String soldText = product.getSoldCount() != null ? "(" + product.getSoldCount() + " đã bán)" : "";
        ((TextView) view.findViewById(R.id.tvSoldCount)).setText(soldText);

        ((TextView) view.findViewById(R.id.tvDescription)).setText(product.getDescription());

        TextView tvNoReview = view.findViewById(R.id.tvNoReview);
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            reviewAdapter.setReviews(product.getReviews());
            tvNoReview.setVisibility(View.GONE);
        } else {
            tvNoReview.setVisibility(View.VISIBLE);
        }
    }
}
