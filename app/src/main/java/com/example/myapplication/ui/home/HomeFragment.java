package com.example.myapplication.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.api.AddressApiService;
import com.example.myapplication.api.CategoryApiService;
import com.example.myapplication.api.ProductApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.AddressResponse;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.CategoryResponse;
import com.example.myapplication.model.ProductResponse;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.ui.detail.ProductDetailFragment;
import com.example.myapplication.ui.order.OptionsBottomSheet;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bumptech.glide.Glide;

public class HomeFragment extends Fragment {

    private ProductCardAdapter bestSellerAdapter;
    private ProductCardAdapter newProductAdapter;
    private ProductCardAdapter recommendAdapter;
    private CategoryAdapter categoryAdapter;
    private ProductCardAdapter searchAdapter;

    private ProductApiService productApiService;
    private CategoryApiService categoryApiService;
    private AddressApiService addressApiService;

    // Search UI
    private LinearLayout layoutSearch;
    private EditText etSearch;
    private ImageView ivSearch, ivClearSearch;
    private RecyclerView rvSearchResults;
    private boolean isSearchOpen = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productApiService  = RetrofitClient.getInstance(requireContext()).create(ProductApiService.class);
        categoryApiService = RetrofitClient.getInstance(requireContext()).create(CategoryApiService.class);
        addressApiService  = RetrofitClient.getInstance(requireContext()).create(AddressApiService.class);

        initSearch(view);
        initRecyclerViews(view);

        // ============================================================
        // CODE LOAD ẢNH BANNER & SETUP BUTTONS (NỘI DUNG THÊM MỚI)
        // ============================================================

        // Load ảnh banner bằng Glide
        ImageView ivBanner = view.findViewById(R.id.ivBanner);
        String bannerUrl = "https://gongcha.com.vn/wp-content/uploads/2020/08/banner-menu-1900-x-335.jpg";

        Glide.with(this)
                .load(bannerUrl)
                .placeholder(R.color.gray) // Ảnh màu xám hiện lên khi đang tải
                .error(R.color.red)       // Ảnh đỏ nếu load lỗi
                .into(ivBanner);

        setupButtons(view);

        // Load tất cả data
        loadDefaultAddress(view);   // ← địa chỉ của user đang đăng nhập
        loadCategories();
        loadBestSellers();
        loadNewProducts();
        loadRecommendations();
    }

    // ================================================================
    // ĐỊA CHỈ GIAO HÀNG — GET /api/address
    // Token tự động gắn qua authInterceptor → BE biết user là ai
    // ================================================================
    private void loadDefaultAddress(View view) {
        TextView tvAddress = view.findViewById(R.id.tvAddress);

        addressApiService.getMyAddresses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<AddressResponse>>> call,
                                   @NonNull Response<BaseResponse<List<AddressResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {

                    List<AddressResponse> addresses = response.body().getData();

                    // Tìm địa chỉ mặc định (isDefault = true)
                    AddressResponse defaultAddress = null;
                    for (AddressResponse addr : addresses) {
                        if (Boolean.TRUE.equals(addr.getIsDefault())) {
                            defaultAddress = addr;
                            break;
                        }
                    }

                    // Nếu có địa chỉ mặc định → hiển thị
                    // Nếu không → lấy địa chỉ đầu tiên trong list
                    if (defaultAddress != null) {
                        tvAddress.setText(defaultAddress.getAddressLine());
                    } else if (!addresses.isEmpty()) {
                        tvAddress.setText(addresses.get(0).getAddressLine());
                    } else {
                        tvAddress.setText("Chua co dia chi");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<AddressResponse>>> call,
                                  @NonNull Throwable t) {
                // Chua dang nhap hoac loi mang → giu nguyen text mac dinh
            }
        });
    }

    // ================================================================
    // SEARCH
    // ================================================================
    private void initSearch(View view) {
        ivSearch        = view.findViewById(R.id.ivSearch);
        layoutSearch    = view.findViewById(R.id.layoutSearch);
        etSearch        = view.findViewById(R.id.etSearch);
        ivClearSearch   = view.findViewById(R.id.ivClearSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);

        searchAdapter = new ProductCardAdapter(ProductCardAdapter.MODE_NORMAL,
                new ProductCardAdapter.OnProductClickListener() {
                    @Override
                    public void onItemClick(ProductResponse product) {
                        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(product.getId());
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, detailFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    @Override
                    public void onAddToCart(ProductResponse product) {
                        addToCartWithAuthGuard(product.getId());
                    }
                });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(searchAdapter);

        ivSearch.setOnClickListener(v -> {
            if (isSearchOpen) closeSearch();
            else openSearch();
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            rvSearchResults.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    searchProducts(keyword);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });
    }

    private void openSearch() {
        isSearchOpen = true;
        layoutSearch.setVisibility(View.VISIBLE);
        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSearch() {
        isSearchOpen = false;
        layoutSearch.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
        etSearch.setText("");
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private void searchProducts(String keyword) {
        Map<String, String> filters = new HashMap<>();
        filters.put("keyword", keyword);
        productApiService.searchProducts(filters).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                   @NonNull Response<BaseResponse<List<ProductResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    List<ProductResponse> results = response.body().getData();
                    searchAdapter.setProducts(results);
                    rvSearchResults.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                    if (results.isEmpty()) {
                        Toast.makeText(getContext(), "Khong tim thay san pham", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                  @NonNull Throwable t) {
                showError("Loi tim kiem");
            }
        });
    }

    // ================================================================
    // BUTTONS
    // ================================================================
    private void setupButtons(View view) {
        view.findViewById(R.id.btnOrderNow).setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_order);
        });
    }

    // ================================================================
    // RECYCLERVIEW
    // ================================================================
    private void initRecyclerViews(View view) {
        categoryAdapter = new CategoryAdapter((category, position) -> {
            if (category.getId() == null) loadBestSellers();
            else filterProductsByCategory(category.getId());
        });
        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        ProductCardAdapter.OnProductClickListener clickListener =
                new ProductCardAdapter.OnProductClickListener() {
                    @Override
                    public void onItemClick(ProductResponse product) {
                        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(product.getId());
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, detailFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    @Override
                    public void onAddToCart(ProductResponse product) {
                        addToCartWithAuthGuard(product.getId());
                    }
                };

        bestSellerAdapter = new ProductCardAdapter(ProductCardAdapter.MODE_RANK, clickListener);
        RecyclerView rvBestSellers = view.findViewById(R.id.rvBestSellers);
        rvBestSellers.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBestSellers.setAdapter(bestSellerAdapter);

        newProductAdapter = new ProductCardAdapter(ProductCardAdapter.MODE_NEW, clickListener);
        RecyclerView rvNewProducts = view.findViewById(R.id.rvNewProducts);
        rvNewProducts.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNewProducts.setAdapter(newProductAdapter);

        recommendAdapter = new ProductCardAdapter(ProductCardAdapter.MODE_NORMAL, clickListener);
        RecyclerView rvRecommendations = view.findViewById(R.id.rvRecommendations);
        rvRecommendations.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setAdapter(recommendAdapter);
    }

    // ================================================================
    // API CALLS
    // ================================================================
    private void loadCategories() {
        categoryApiService.getCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<CategoryResponse>>> call,
                                   @NonNull Response<BaseResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    categoryAdapter.setCategories(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<CategoryResponse>>> call,
                                  @NonNull Throwable t) {
                showError("Khong tai duoc danh muc");
            }
        });
    }

    private void loadBestSellers() {
        productApiService.getBestSellers().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                   @NonNull Response<BaseResponse<List<ProductResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    bestSellerAdapter.setProducts(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                  @NonNull Throwable t) {
                showError("Khong tai duoc san pham ban chay");
            }
        });
    }

    private void loadNewProducts() {
        Map<String, String> filters = new HashMap<>();
        filters.put("sortBy", "newest");
        productApiService.searchProducts(filters).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                   @NonNull Response<BaseResponse<List<ProductResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    newProductAdapter.setProducts(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                  @NonNull Throwable t) {
                showError("Khong tai duoc san pham moi");
            }
        });
    }

    private void loadRecommendations() {
        productApiService.getRecommendations().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                   @NonNull Response<BaseResponse<List<ProductResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    recommendAdapter.setProducts(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                  @NonNull Throwable t) {
                showError("Khong tai duoc san pham goi y");
            }
        });
    }

    private void filterProductsByCategory(Integer categoryId) {
        Map<String, String> filters = new HashMap<>();
        filters.put("categoryId", String.valueOf(categoryId));
        productApiService.searchProducts(filters).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                   @NonNull Response<BaseResponse<List<ProductResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    bestSellerAdapter.setProducts(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ProductResponse>>> call,
                                  @NonNull Throwable t) {
                showError("Khong loc duoc san pham");
            }
        });
    }

    private void addToCartWithAuthGuard(int productId) {
        TokenManager tokenManager = new TokenManager(requireContext());
        if (tokenManager.getToken() == null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("PendingCart", android.app.Activity.MODE_PRIVATE);
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

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}