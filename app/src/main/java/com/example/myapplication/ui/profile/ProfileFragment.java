package com.example.myapplication.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.Address;
import com.example.myapplication.model.AddressRequest;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderItem;
import com.example.myapplication.model.UpdateProfileRequest;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.TokenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FrameLayout frameAvatar;
    private ImageView ivAvatar;
    private TextView tvFullName, tvEmail, tvPhone, tvEmptyAddress, tvTopProduct, tvEmptyStats;
    private RecyclerView rvAddresses;
    private LinearLayout layoutToppingStats;
    private View btnEditProfile, btnAddAddress, btnLogout;

    private AddressAdapter addressAdapter;
    private ApiService apiService;
    private TokenManager tokenManager;
    private UserProfile currentProfile;

    // Launcher chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // Hiển thị ngay lập tức bằng Glide
                        Glide.with(requireContext())
                                .load(imageUri)
                                .circleCrop()
                                .into(ivAvatar);
                        // Lưu URI dưới dạng string để upload
                        uploadAvatarFromUri(imageUri);
                    }
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        frameAvatar     = view.findViewById(R.id.frameAvatar);
        ivAvatar        = view.findViewById(R.id.ivAvatar);
        tvFullName      = view.findViewById(R.id.tvFullName);
        tvEmail         = view.findViewById(R.id.tvEmail);
        tvPhone         = view.findViewById(R.id.tvPhone);
        tvEmptyAddress  = view.findViewById(R.id.tvEmptyAddress);
        tvTopProduct    = view.findViewById(R.id.tvTopProduct);
        tvEmptyStats    = view.findViewById(R.id.tvEmptyStats);
        rvAddresses     = view.findViewById(R.id.rvAddresses);
        layoutToppingStats = view.findViewById(R.id.layoutToppingStats);
        btnEditProfile  = view.findViewById(R.id.btnEditProfile);
        btnAddAddress   = view.findViewById(R.id.btnAddAddress);
        btnLogout       = view.findViewById(R.id.btnLogout);

        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        addressAdapter = new AddressAdapter(new ArrayList<>());
        rvAddresses.setAdapter(addressAdapter);

        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        btnLogout.setOnClickListener(v -> logout());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());

        // Bấm vào avatar → chọn ảnh từ thư viện
        frameAvatar.setOnClickListener(v -> openImagePicker());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProfile();
        loadAddresses();
        loadOrdersAndStats();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void uploadAvatarFromUri(Uri uri) {
        // Dùng URI làm avatarUrl tạm — trong thực tế cần upload lên cloud storage
        // rồi lấy URL, ở đây ta dùng content URI convert thành string
        String uriStr = uri.toString();
        String name = currentProfile != null ? currentProfile.getFullName() : "";
        String phone = currentProfile != null ? currentProfile.getPhoneNumber() : "";

        UpdateProfileRequest request = new UpdateProfileRequest(name, phone, uriStr);
        apiService.updateProfile(request).enqueue(new Callback<BaseResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserProfile>> call, Response<BaseResponse<UserProfile>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentProfile = response.body().getData();
                    Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    // Dù backend có lỗi, ảnh vẫn hiển thị local (Glide đã load)
                    Toast.makeText(getContext(), "Ảnh đã cập nhật trên thiết bị", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserProfile>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Ảnh đã cập nhật trên thiết bị", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        apiService.getProfile().enqueue(new Callback<BaseResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserProfile>> call, Response<BaseResponse<UserProfile>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body().getData();
                    if (profile != null) {
                        currentProfile = profile;
                        tvFullName.setText(profile.getFullName() != null ? profile.getFullName() : "");
                        tvEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
                        tvPhone.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Chưa cập nhật");
                        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                            Glide.with(requireContext()).load(profile.getAvatarUrl()).circleCrop().into(ivAvatar);
                        }
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    Toast.makeText(getContext(), "Không thể tải thông tin cá nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserProfile>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Không thể kết nối máy chủ. Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAddresses() {
        apiService.getAddresses().enqueue(new Callback<BaseResponse<List<Address>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Address>>> call, Response<BaseResponse<List<Address>>> response) {
                if (!isAdded()) return;
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
                if (!isAdded()) return;
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
            layoutToppingStats.setVisibility(View.GONE);
            tvTopProduct.setVisibility(View.GONE);
            return;
        }

        Map<String, Integer> productCount = new HashMap<>();
        boolean hasCompleted = false;

        for (Order order : orders) {
            if ("COMPLETED".equals(order.getOrderStatus())) {
                hasCompleted = true;
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        String pName = item.getProductName();
                        if (pName != null) {
                            productCount.put(pName, productCount.getOrDefault(pName, 0) + item.getQuantity());
                        }
                    }
                }
            }
        }

        if (!hasCompleted) {
            tvEmptyStats.setVisibility(View.VISIBLE);
            layoutToppingStats.setVisibility(View.GONE);
            tvTopProduct.setVisibility(View.GONE);
            return;
        }

        tvEmptyStats.setVisibility(View.GONE);
        tvTopProduct.setVisibility(View.VISIBLE);

        String topProduct = "N/A";
        int maxP = 0;
        for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
            if (entry.getValue() > maxP) {
                maxP = entry.getValue();
                topProduct = entry.getKey();
            }
        }
        tvTopProduct.setText("Sản phẩm mua nhiều nhất: " + topProduct + " (" + maxP + " lượt)");
        layoutToppingStats.setVisibility(View.GONE);
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etPhone = dialogView.findViewById(R.id.etEditPhone);

        if (currentProfile != null) {
            etName.setText(currentProfile.getFullName());
            etPhone.setText(currentProfile.getPhoneNumber());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật thông tin")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateProfile(name, phone);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateProfile(String fullName, String phoneNumber) {
        String avatarUrl = currentProfile != null ? currentProfile.getAvatarUrl() : null;
        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phoneNumber, avatarUrl);
        apiService.updateProfile(request).enqueue(new Callback<BaseResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserProfile>> call, Response<BaseResponse<UserProfile>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile updated = response.body().getData();
                    if (updated != null) {
                        currentProfile = updated;
                        tvFullName.setText(updated.getFullName());
                        tvPhone.setText(updated.getPhoneNumber() != null ? updated.getPhoneNumber() : "Chưa cập nhật");
                    }
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserProfile>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddAddressDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_address, null);
        EditText etAddress = dialogView.findViewById(R.id.etAddressLine);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm địa chỉ mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String addressLine = etAddress.getText().toString().trim();
                    if (addressLine.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addAddress(addressLine);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addAddress(String addressLine) {
        com.example.myapplication.model.AddressRequest request = new AddressRequest(addressLine, false);
        apiService.addAddress(request).enqueue(new Callback<BaseResponse<Address>>() {
            @Override
            public void onResponse(Call<BaseResponse<Address>> call, Response<BaseResponse<Address>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                } else if (response.code() == 401 || response.code() == 403) {
                    goToLogin();
                } else {
                    Toast.makeText(getContext(), "Thêm địa chỉ thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Address>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    apiService.logout().enqueue(new Callback<BaseResponse<Void>>() {
                        @Override
                        public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                            goToLogin();
                        }

                        @Override
                        public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                            goToLogin();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
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
