package com.example.myapplication.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.api.CategoryApiService;
import com.example.myapplication.api.ProductApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.CategoryResponse;
import com.example.myapplication.model.ProductResponse;
import com.example.myapplication.ui.order.OptionsBottomSheet;
import com.example.myapplication.ui.product.ProductDetailActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuFragment extends Fragment {

    // UI
    private EditText etSearch;
    private LinearLayout llCategories;
    private CardView cardFilter;
    private EditText etMinPrice, etMaxPrice;
    private Spinner spinnerSort;
    private LinearLayout llRatingFilter;
    private TextView tvProductCount, tvClearFilter, tvCartBadge;
    private RecyclerView rvProducts;

    // Data
    private MenuProductAdapter adapter;
    private List<CategoryResponse> categories = new ArrayList<>();
    private Integer selectedCategoryId = null; // null = Tất cả
    private Integer selectedMinRating = null;
    private boolean filterVisible = false;

    // Sort options
    private static final String[] SORT_LABELS = {"Phổ biến nhất", "Giá: Thấp đến cao", "Giá: Cao đến thấp"};
    private static final String[] SORT_VALUES = {"sold_desc", "price_asc", "price_desc"};

    // Debounce search
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etSearch = view.findViewById(R.id.etSearch);
        llCategories = view.findViewById(R.id.llCategories);
        cardFilter = view.findViewById(R.id.cardFilter);
        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        llRatingFilter = view.findViewById(R.id.llRatingFilter);
        tvProductCount = view.findViewById(R.id.tvProductCount);
        tvClearFilter = view.findViewById(R.id.tvClearFilter);
        tvCartBadge = view.findViewById(R.id.tvCartBadge);
        rvProducts = view.findViewById(R.id.rvProducts);

        setupRecyclerView();
        setupSort();
        setupRatingFilter();
        setupSearchBar();
        setupFilterToggle(view);

        view.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            cardFilter.setVisibility(View.GONE);
            filterVisible = false;
            loadProducts();
        });

        tvClearFilter.setOnClickListener(v -> clearFilter());

        view.findViewById(R.id.flCartIcon).setOnClickListener(v -> {
            // Navigate to CartFragment via MainActivity
            if (getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.myapplication.ui.cart.CartFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadCategories();
    }

    private void setupRecyclerView() {
        adapter = new MenuProductAdapter(new MenuProductAdapter.Listener() {
            @Override
            public void onProductClick(int productId) {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId);
                startActivity(intent);
            }

            @Override
            public void onAddClick(int productId) {
                OptionsBottomSheet sheet = new OptionsBottomSheet(productId);
                sheet.show(getParentFragmentManager(), "options");
            }
        });
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);
    }

    private void setupSort() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, SORT_LABELS);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {}
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupRatingFilter() {
        String[] stars = {"★1", "★2", "★3", "★4", "★5"};
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            TextView chip = new TextView(getContext());
            chip.setText(stars[i]);
            chip.setTextSize(12f);
            chip.setPadding(14, 8, 14, 8);
            chip.setTextColor(0xFF666666);
            chip.setBackgroundResource(R.drawable.bg_category_normal);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                if (selectedMinRating != null && selectedMinRating == rating) {
                    // deselect
                    selectedMinRating = null;
                    chip.setBackgroundResource(R.drawable.bg_category_normal);
                    chip.setTextColor(0xFF666666);
                } else {
                    selectedMinRating = rating;
                    resetRatingChips();
                    chip.setBackgroundResource(R.drawable.bg_category_selected);
                    chip.setTextColor(0xFFE8622A);
                }
            });
            llRatingFilter.addView(chip);
        }
    }

    private void resetRatingChips() {
        for (int i = 0; i < llRatingFilter.getChildCount(); i++) {
            View child = llRatingFilter.getChildAt(i);
            child.setBackgroundResource(R.drawable.bg_category_normal);
            if (child instanceof TextView) ((TextView) child).setTextColor(0xFF666666);
        }
    }

    private void setupSearchBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> loadProducts();
                searchHandler.postDelayed(searchRunnable, 400);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterToggle(View root) {
        root.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            filterVisible = !filterVisible;
            cardFilter.setVisibility(filterVisible ? View.VISIBLE : View.GONE);
        });
    }

    private void loadCategories() {
        CategoryApiService api = RetrofitClient.getInstance(requireContext())
                .create(CategoryApiService.class);
        api.getCategories().enqueue(new Callback<BaseResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<CategoryResponse>>> call,
                                   @NonNull Response<BaseResponse<List<CategoryResponse>>> response) {
                if (!isAdded()) return;
                categories.clear();

                // "Tất cả" item
                CategoryResponse all = new CategoryResponse();
                all.setId(null);
                all.setName("Tất cả");
                categories.add(all);

                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    categories.addAll(response.body().getData());
                }
                buildCategoryChips();
                loadProducts();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<CategoryResponse>>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                CategoryResponse all = new CategoryResponse();
                all.setId(null);
                all.setName("Tất cả");
                categories.add(all);
                buildCategoryChips();
                loadProducts();
            }
        });
    }

    private void buildCategoryChips() {
        llCategories.removeAllViews();
        String[] emojis = {"🧋", "🥛", "🍊", "🌿", "🧊", "🫧", "☕", "🍵"};

        for (int i = 0; i < categories.size(); i++) {
            CategoryResponse cat = categories.get(i);
            String emoji = i < emojis.length ? emojis[i] : "☕";

            View chipView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_category_tab, llCategories, false);
            TextView tvIcon = chipView.findViewById(R.id.tvCategoryIcon);
            TextView tvName = chipView.findViewById(R.id.tvCategoryName);
            FrameLayout flBg = chipView.findViewById(R.id.flCategoryBg);

            tvIcon.setText(emoji);
            tvName.setText(cat.getName());

            boolean isSelected = (cat.getId() == null && selectedCategoryId == null)
                    || (cat.getId() != null && cat.getId().equals(selectedCategoryId));
            flBg.setBackgroundResource(isSelected
                    ? R.drawable.bg_category_selected
                    : R.drawable.bg_category_normal);
            tvName.setTextColor(isSelected ? 0xFFE8622A : 0xFF888888);

            final Integer catId = cat.getId();
            chipView.setOnClickListener(v -> {
                selectedCategoryId = catId;
                buildCategoryChips();
                loadProducts();
            });

            llCategories.addView(chipView);
        }
    }

    private void loadProducts() {
        if (!isAdded()) return;
        tvProductCount.setText("Đang tải...");

        String keyword = etSearch.getText().toString().trim();
        String minP = etMinPrice.getText().toString().trim().replace(".", "");
        String maxP = etMaxPrice.getText().toString().trim().replace(".", "");
        boolean hasFilter = !keyword.isEmpty() || selectedCategoryId != null
                || !minP.isEmpty() || !maxP.isEmpty() || selectedMinRating != null
                || spinnerSort.getSelectedItemPosition() > 0;

        ProductApiService api = RetrofitClient.getInstance(requireContext())
                .create(ProductApiService.class);

        Callback<BaseResponse<List<ProductResponse>>> cb =
                new Callback<BaseResponse<List<ProductResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                           @NonNull Response<BaseResponse<List<ProductResponse>>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            List<ProductResponse> list = response.body().getData();
                            adapter.setItems(list);
                            tvProductCount.setText(list.size() + " sản phẩm");
                        } else {
                            adapter.setItems(new ArrayList<>());
                            tvProductCount.setText("0 sản phẩm");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                         @NonNull Throwable t) {
                        if (!isAdded()) return;
                        tvProductCount.setText("Lỗi tải dữ liệu");
                        Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                };

        if (!hasFilter) {
            // Không có filter → lấy tất cả sản phẩm
            api.getAllProducts().enqueue(cb);
        } else {
            // Có filter → dùng search endpoint
            Map<String, String> filters = new HashMap<>();
            if (!keyword.isEmpty()) filters.put("keyword", keyword);
            if (selectedCategoryId != null) filters.put("categoryId", String.valueOf(selectedCategoryId));
            if (!minP.isEmpty()) filters.put("minPrice", minP);
            if (!maxP.isEmpty()) filters.put("maxPrice", maxP);
            if (selectedMinRating != null) filters.put("minRating", String.valueOf(selectedMinRating));
            if (spinnerSort.getSelectedItemPosition() > 0)
                filters.put("sortBy", SORT_VALUES[spinnerSort.getSelectedItemPosition()]);
            api.searchProducts(filters).enqueue(cb);
        }
    }

    private void clearFilter() {
        etMinPrice.setText("");
        etMaxPrice.setText("");
        spinnerSort.setSelection(0);
        selectedMinRating = null;
        resetRatingChips();
        loadProducts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }
}
