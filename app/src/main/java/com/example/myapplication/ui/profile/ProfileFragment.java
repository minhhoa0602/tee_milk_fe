package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.Address;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderItem;
import com.example.myapplication.model.OrderItemTopping;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.utils.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvFullName, tvEmail, tvPhone, tvEmptyAddress, tvTopProduct, tvEmptyStats;
    private RecyclerView rvAddresses;
    private LinearLayout layoutToppingStats;
    private View btnEditProfile, btnAddAddress, btnLogout;

    private AddressAdapter addressAdapter;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmptyAddress = view.findViewById(R.id.tvEmptyAddress);
        tvTopProduct = view.findViewById(R.id.tvTopProduct);
        tvEmptyStats = view.findViewById(R.id.tvEmptyStats);
        rvAddresses = view.findViewById(R.id.rvAddresses);
        layoutToppingStats = view.findViewById(R.id.layoutToppingStats);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnAddAddress = view.findViewById(R.id.btnAddAddress);
        btnLogout = view.findViewById(R.id.btnLogout);

        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        addressAdapter = new AddressAdapter(new ArrayList<>());
        rvAddresses.setAdapter(addressAdapter);

        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        btnLogout.setOnClickListener(v -> logout());
        btnEditProfile.setOnClickListener(v -> editProfile());
        btnAddAddress.setOnClickListener(v -> addAddress());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProfile();
        loadAddresses();
        loadOrdersAndStats();
    }

    private void loadProfile() {
        apiService.getProfile().enqueue(new Callback<BaseResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserProfile>> call, Response<BaseResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body().getData();
                    if (profile != null) {
                        tvFullName.setText(profile.getFullName());
                        tvEmail.setText(profile.getEmail());
                        tvPhone.setText(profile.getPhone());
                        // TODO: Load avatar using Glide or Picasso if available
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserProfile>> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAddresses() {
        apiService.getAddresses().enqueue(new Callback<BaseResponse<List<Address>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Address>>> call, Response<BaseResponse<List<Address>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Address> addresses = response.body().getData();
                    if (addresses == null || addresses.isEmpty()) {
                        tvEmptyAddress.setVisibility(View.VISIBLE);
                        rvAddresses.setVisibility(View.GONE);
                    } else {
                        tvEmptyAddress.setVisibility(View.GONE);
                        rvAddresses.setVisibility(View.VISIBLE);
                        addressAdapter.setAddressList(addresses);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Address>>> call, Throwable t) {}
        });
    }

    private void loadOrdersAndStats() {
        apiService.getOrders("ALL").enqueue(new Callback<BaseResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Order>>> call, Response<BaseResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    calculateStats(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Order>>> call, Throwable t) {}
        });
    }

    private void calculateStats(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            tvEmptyStats.setVisibility(View.VISIBLE);
            return;
        }

        Map<String, Integer> productCount = new HashMap<>();
        Map<String, Integer> toppingCount = new HashMap<>();
        int totalToppings = 0;
        boolean hasCompleted = false;

        for (Order order : orders) {
            if ("COMPLETED".equals(order.getStatus())) {
                hasCompleted = true;
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        String pName = item.getProductName();
                        productCount.put(pName, productCount.getOrDefault(pName, 0) + item.getQuantity());

                        if (item.getToppings() != null) {
                            for (OrderItemTopping topping : item.getToppings()) {
                                String tName = topping.getToppingName();
                                toppingCount.put(tName, toppingCount.getOrDefault(tName, 0) + 1);
                                totalToppings++;
                            }
                        }
                    }
                }
            }
        }

        if (!hasCompleted) {
            tvEmptyStats.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyStats.setVisibility(View.GONE);

        // Top product
        String topProduct = "N/A";
        int maxP = 0;
        for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
            if (entry.getValue() > maxP) {
                maxP = entry.getValue();
                topProduct = entry.getKey();
            }
        }
        tvTopProduct.setText("Sản phẩm mua nhiều nhất: " + topProduct + " (" + maxP + " lượt)");

        // Topping stats
        layoutToppingStats.removeAllViews();
        for (Map.Entry<String, Integer> entry : toppingCount.entrySet()) {
            double percent = (entry.getValue() * 100.0) / totalToppings;
            TextView tv = new TextView(getContext());
            tv.setText(String.format("%s: %.1f%%", entry.getKey(), percent));
            layoutToppingStats.addView(tv);
        }
    }

    private void editProfile() {
        // Implement logic to show a dialog or navigate to edit profile screen
        Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void addAddress() {
        // Implement logic to show a dialog for adding address
        Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        apiService.logout().enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                tokenManager.clearToken();
                handleUnauthorized();
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                tokenManager.clearToken();
                handleUnauthorized();
            }
        });
    }

    private void handleUnauthorized() {
        // Clear token and move to LoginActivity
        tokenManager.clearToken();
        if (getActivity() != null) {
            getActivity().finish();
            // Start LoginActivity if it exists. For now, just exit or show toast.
            Toast.makeText(getContext(), "Phiên làm việc hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
        }
    }
}
