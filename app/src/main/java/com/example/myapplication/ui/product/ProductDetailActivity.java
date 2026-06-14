package com.example.myapplication.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.CartRequest;
import com.example.myapplication.model.ProductDetail;
import com.example.myapplication.model.ProductOptions;
import com.example.myapplication.model.SizeOption;
import com.example.myapplication.model.ToppingOption;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private android.widget.ImageView ivProductImage;
    private android.widget.ImageButton btnBack;
    private TextView tvProductName, tvProductPrice, tvRatingValue, tvSoldCount, tvProductDescription, tvEmptyReview;
    private RatingBar ratingBarProduct;
    private RecyclerView rvReviews;
    private Button btnChooseOptions;

    private ReviewAdapter reviewAdapter;
    private ApiService apiService;
    private TokenManager tokenManager;

    private ProductDetail currentProduct;
    private ProductOptions currentOptions;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) { finish(); return; }

        initViews();
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        tokenManager = new TokenManager(this);

        btnBack.setOnClickListener(v -> finish());
        btnChooseOptions.setOnClickListener(v -> openChooseOptions());

        loadProductDetail();
    }

    private void initViews() {
        ivProductImage      = findViewById(R.id.ivProductImage);
        btnBack             = findViewById(R.id.btnBack);
        tvProductName       = findViewById(R.id.tvProductName);
        tvProductPrice      = findViewById(R.id.tvProductPrice);
        tvRatingValue       = findViewById(R.id.tvRatingValue);
        tvSoldCount         = findViewById(R.id.tvSoldCount);
        tvProductDescription= findViewById(R.id.tvProductDescription);
        tvEmptyReview       = findViewById(R.id.tvEmptyReview);
        ratingBarProduct    = findViewById(R.id.ratingBarProduct);
        rvReviews           = findViewById(R.id.rvReviews);
        btnChooseOptions    = findViewById(R.id.btnChooseOptions);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadProductDetail() {
        apiService.getProductDetail(productId).enqueue(new Callback<BaseResponse<ProductDetail>>() {
            @Override
            public void onResponse(Call<BaseResponse<ProductDetail>> call, Response<BaseResponse<ProductDetail>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentProduct = response.body().getData();
                    bindProduct(currentProduct);
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ProductDetail>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProduct(ProductDetail p) {
        tvProductName.setText(p.getName() != null ? p.getName() : "");
        tvProductPrice.setText(p.getBasePrice() != null ?
                String.format("%,.0fđ", p.getBasePrice().doubleValue()) : "");
        double rating = p.getAverageRating();
        ratingBarProduct.setRating((float) rating);
        tvRatingValue.setText(String.format("%.1f (%d đánh giá)",
                rating, p.getReviews() != null ? p.getReviews().size() : 0));
        tvSoldCount.setText("Đã bán: " + p.getSoldCount() + " lượt");
        tvProductDescription.setText(p.getDescription() != null ? p.getDescription() : "Chưa có mô tả");

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Glide.with(this).load(p.getImageUrl()).centerCrop().into(ivProductImage);
        }

        if (p.getReviews() == null || p.getReviews().isEmpty()) {
            tvEmptyReview.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
        } else {
            tvEmptyReview.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
            reviewAdapter.setReviews(p.getReviews());
        }
    }

    private void openChooseOptions() {
        if (currentProduct == null) return;

        if (currentOptions != null) {
            showChooseDialog(currentOptions);
            return;
        }

        apiService.getProductOptions(productId).enqueue(new Callback<BaseResponse<ProductOptions>>() {
            @Override
            public void onResponse(Call<BaseResponse<ProductOptions>> call, Response<BaseResponse<ProductOptions>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentOptions = response.body().getData();
                    showChooseDialog(currentOptions);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không thể tải tùy chọn sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ProductOptions>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChooseDialog(ProductOptions options) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_choose_options, null);

        TextView tvDialogName   = dialogView.findViewById(R.id.tvDialogProductName);
        ChipGroup cgSize        = dialogView.findViewById(R.id.chipGroupSize);
        ChipGroup cgIce         = dialogView.findViewById(R.id.chipGroupIce);
        ChipGroup cgSugar       = dialogView.findViewById(R.id.chipGroupSugar);
        ChipGroup cgTopping     = dialogView.findViewById(R.id.chipGroupTopping);
        Button btnDecrease      = dialogView.findViewById(R.id.btnDecreaseQty);
        Button btnIncrease      = dialogView.findViewById(R.id.btnIncreaseQty);
        TextView tvQty          = dialogView.findViewById(R.id.tvQuantity);
        TextView tvTotal        = dialogView.findViewById(R.id.tvTotalPrice);
        Button btnAddToCart     = dialogView.findViewById(R.id.btnAddToCart);
        Button btnBuyNow        = dialogView.findViewById(R.id.btnBuyNow);

        tvDialogName.setText(currentProduct.getName());

        // --- SIZE chips ---
        cgSize.setSingleSelection(true);
        List<SizeOption> sizes = options.getSizes();
        if (sizes != null) {
            for (SizeOption s : sizes) {
                Chip chip = makeChip(s.getName() + (s.getPriceAdd().compareTo(BigDecimal.ZERO) > 0
                        ? " +" + String.format("%,.0fđ", s.getPriceAdd().doubleValue()) : ""));
                chip.setTag(s);
                cgSize.addView(chip);
            }
            if (cgSize.getChildCount() > 0) ((Chip) cgSize.getChildAt(0)).setChecked(true);
        }

        // --- ICE chips ---
        cgIce.setSingleSelection(true);
        addLevelChips(cgIce, options.getIceLevels(), new String[]{"Không đá", "Ít đá", "Bình thường"});

        // --- SUGAR chips ---
        cgSugar.setSingleSelection(true);
        addLevelChips(cgSugar, options.getSugarLevels(), new String[]{"Không đường", "Ít đường", "Bình thường"});

        // --- TOPPING chips (multi-select) ---
        List<ToppingOption> toppings = options.getToppings();
        if (toppings != null) {
            for (ToppingOption t : toppings) {
                Chip chip = makeChip(t.getName() + " +" + String.format("%,.0fđ", t.getPrice().doubleValue()));
                chip.setCheckable(true);
                chip.setTag(t);
                cgTopping.addView(chip);
            }
        }
        // ChipGroup topping cho phép chọn nhiều — không đặt SingleSelection
        cgTopping.setSingleSelection(false);

        // --- Số lượng & tổng tiền ---
        final int[] qty = {1};

        Runnable calcTotal = () -> {
            BigDecimal base = currentProduct.getBasePrice() != null ? currentProduct.getBasePrice() : BigDecimal.ZERO;

            // Size
            int sizeChecked = cgSize.getCheckedChipId();
            if (sizeChecked != View.NO_ID) {
                Chip c = dialogView.findViewById(sizeChecked);
                if (c != null && c.getTag() instanceof SizeOption) {
                    base = base.add(((SizeOption) c.getTag()).getPriceAdd());
                }
            }

            // Toppings
            List<Integer> checkedToppingIds = cgTopping.getCheckedChipIds();
            for (int cid : checkedToppingIds) {
                Chip c = dialogView.findViewById(cid);
                if (c != null && c.getTag() instanceof ToppingOption) {
                    base = base.add(((ToppingOption) c.getTag()).getPrice());
                }
            }

            double total = base.doubleValue() * qty[0];
            tvTotal.setText(String.format("%,.0fđ", total));
        };

        cgSize.setOnCheckedStateChangeListener((g, ids) -> calcTotal.run());
        cgTopping.setOnCheckedStateChangeListener((g, ids) -> calcTotal.run());
        calcTotal.run();

        btnDecrease.setOnClickListener(v -> {
            if (qty[0] > 1) { qty[0]--; tvQty.setText(String.valueOf(qty[0])); calcTotal.run(); }
        });
        btnIncrease.setOnClickListener(v -> {
            qty[0]++; tvQty.setText(String.valueOf(qty[0])); calcTotal.run();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnAddToCart.setOnClickListener(v -> {
            CartRequest req = buildCartRequest(cgSize, cgIce, cgSugar, cgTopping, qty[0], dialogView);
            if (req == null) return;
            addToCart(req, false, dialog);
        });

        btnBuyNow.setOnClickListener(v -> {
            CartRequest req = buildCartRequest(cgSize, cgIce, cgSugar, cgTopping, qty[0], dialogView);
            if (req == null) return;
            addToCart(req, true, dialog);
        });

        dialog.show();
    }

    private CartRequest buildCartRequest(ChipGroup cgSize, ChipGroup cgIce, ChipGroup cgSugar,
                                         ChipGroup cgTopping, int qty, View root) {
        int sizeChecked = cgSize.getCheckedChipId();
        if (sizeChecked == View.NO_ID) {
            Toast.makeText(this, "Vui lòng chọn kích cỡ", Toast.LENGTH_SHORT).show();
            return null;
        }
        Chip sizeChip = root.findViewById(sizeChecked);
        SizeOption selectedSize = (SizeOption) sizeChip.getTag();

        int iceChecked = cgIce.getCheckedChipId();
        String iceLevel = "NORMAL";
        if (iceChecked != View.NO_ID) {
            Chip c = root.findViewById(iceChecked);
            if (c != null) iceLevel = (String) c.getTag();
        }

        int sugarChecked = cgSugar.getCheckedChipId();
        String sugarLevel = "NORMAL";
        if (sugarChecked != View.NO_ID) {
            Chip c = root.findViewById(sugarChecked);
            if (c != null) sugarLevel = (String) c.getTag();
        }

        List<Integer> toppingIds = new ArrayList<>();
        for (int cid : cgTopping.getCheckedChipIds()) {
            Chip c = root.findViewById(cid);
            if (c != null && c.getTag() instanceof ToppingOption) {
                toppingIds.add(((ToppingOption) c.getTag()).getId());
            }
        }

        return new CartRequest(productId, selectedSize.getId(), iceLevel, sugarLevel, toppingIds, qty);
    }

    private void addToCart(CartRequest req, boolean buyNow, AlertDialog dialog) {
        apiService.addToCart(req).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    if (buyNow) {
                        // TODO: chuyển sang màn thanh toán khi có
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ — chuyển thanh toán", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    dialog.dismiss();
                    goToLogin();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không thể thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo chip đơn giản (single-selection mặc định của ChipGroup)
    private Chip makeChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        return chip;
    }

    private void addLevelChips(ChipGroup group, List<String> levels, String[] labels) {
        if (levels == null) return;
        for (int i = 0; i < levels.size(); i++) {
            String level = levels.get(i);
            String label = (i < labels.length) ? labels[i] : level;
            Chip chip = makeChip(label);
            chip.setTag(level); // tag = "NONE" / "LESS" / "NORMAL"
            group.addView(chip);
        }
        if (group.getChildCount() > 0) ((Chip) group.getChildAt(0)).setChecked(true);
    }

    private void goToLogin() {
        tokenManager.clearToken();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
