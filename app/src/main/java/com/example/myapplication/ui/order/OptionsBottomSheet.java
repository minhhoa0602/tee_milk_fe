package com.example.myapplication.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.api.CartApi;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.*;
import com.example.myapplication.model.cart.AddToCartRequest;
import com.example.myapplication.model.cart.CartItem;
import com.example.myapplication.model.cart.UpdateCartRequest;
import com.example.myapplication.model.order.ProductOptionsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OptionsBottomSheet extends BottomSheetDialogFragment {

    // Khai báo UI
    private ImageView ivProductThumb, btnClose;
    private TextView tvProductName, tvBasePrice, tvQuantity;
    private RadioGroup rgSizes, rgSugar, rgIce;
    private LinearLayout llToppings;
    private Button btnPlus, btnMinus, btnAddToCart;

    // Biến logic
    private int requestProductId; // ID truyền từ màn hình ngoài vào
    private int quantity = 1;
    private BigDecimal basePrice = BigDecimal.ZERO;
    private ProductOptionsResponse apiData;

    // Biến lưu cấu hình User chọn
    private Size selectedSize = null;
    private String selectedIce = "NORMAL";
    private String selectedSugar = "NORMAL";
    private Map<Integer, Topping> toppingMap = new HashMap<>(); // ID Checkbox -> Topping object
    private Map<Integer, Boolean> selectedToppings = new HashMap<>(); // ID Topping -> true/false

    // Constructor 1: Dùng cho nút (+) ở màn Trang chủ (THÊM MỚI)
    public OptionsBottomSheet(int productId) {
        this.requestProductId = productId;
        this.isEditMode = false;
    }

    // Constructor 2: Dùng cho nút Chỉnh sửa ở Giỏ hàng (SỬA)
    public OptionsBottomSheet(int productId, CartItem item) {
        this.requestProductId = productId;
        this.editItemData = item;
        this.isEditMode = true;
        this.quantity = item.getQuantity(); // Set luôn số lượng cũ
    }

    // Thêm các biến này vào dưới requestProductId
    private boolean isEditMode = false;
    private CartItem editItemData = null; // Chứa dữ liệu giỏ hàng cũ truyền sang

    // Giao tiếp với Fragment để báo nó tải lại danh sách khi sửa xong
    public interface BottomSheetListener { void onCartUpdated(); }
    private BottomSheetListener updateListener;
    public void setUpdateListener(BottomSheetListener listener) { this.updateListener = listener; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_bottom_sheet_options, container, false);

        ivProductThumb = v.findViewById(R.id.ivProductThumb);
        btnClose = v.findViewById(R.id.btnClose);
        tvProductName = v.findViewById(R.id.tvProductName);
        tvBasePrice = v.findViewById(R.id.tvBasePrice);
        tvQuantity = v.findViewById(R.id.tvQuantity);
        rgSizes = v.findViewById(R.id.rgSizes);
        rgSugar = v.findViewById(R.id.rgSugar);
        rgIce = v.findViewById(R.id.rgIce);
        llToppings = v.findViewById(R.id.llToppings);
        btnPlus = v.findViewById(R.id.btnPlus);
        btnMinus = v.findViewById(R.id.btnMinus);
        btnAddToCart = v.findViewById(R.id.btnAddToCart);

        btnClose.setOnClickListener(view -> dismiss());

        // Disable nút đến khi options load xong
        btnAddToCart.setEnabled(false);
        btnAddToCart.setText("Đang tải...");

        // Gọi API
        fetchData();

        // Nút tăng/giảm số lượng
        btnPlus.setOnClickListener(view -> { quantity++; updateUIPrice(); });
        btnMinus.setOnClickListener(view -> { if(quantity > 1) { quantity--; updateUIPrice(); } });

        // Nút chốt thêm vào giỏ
        btnAddToCart.setOnClickListener(view -> callAddToCartApi());

        return v;
    }

    private void fetchData() {
        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);
        api.getProductOptions(requestProductId).enqueue(new Callback<BaseResponse<ProductOptionsResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<ProductOptionsResponse>> call, Response<BaseResponse<ProductOptionsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    apiData = response.body().getData();
                    renderUI();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse<ProductOptionsResponse>> call, Throwable t) {
                showToast("Lỗi tải dữ liệu mạng");
            }
        });
    }

    private void renderUI() {
        if (apiData == null) return;

        // 1. Header (Tên, Ảnh, Giá Gốc)
        tvProductName.setText(apiData.getProductName());
        this.basePrice = apiData.getBasePrice();
        tvBasePrice.setText(formatMoney(this.basePrice));
        Glide.with(this).load(apiData.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivProductThumb);

        // Enable nút sau khi data đã có
        btnAddToCart.setEnabled(true);

        // 2. Render Sizes (Tích sẵn Size cũ)
        rgSizes.removeAllViews();
        List<Size> sizes = apiData.getSizes() != null ? apiData.getSizes() : new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            Size size = sizes.get(i);
            RadioButton rb = new RadioButton(getContext());
            String label = "[" + size.getName() + "] (+" + formatMoney(size.getPriceAdd()) + ")";
            rb.setText(label);
            rb.setId(View.generateViewId());
            rgSizes.addView(rb);

            // LOGIC TỰ ĐỘNG TICK SIZE
            if (isEditMode && editItemData.getProductSize() != null
                    && editItemData.getProductSize().equals(size.getName())) {
                rb.setChecked(true); selectedSize = size;
            } else if (!isEditMode && i == 0) {
                rb.setChecked(true); selectedSize = size;
            }

            rb.setOnCheckedChangeListener((btn, isChecked) -> { if(isChecked) { selectedSize = size; updateUIPrice(); }});
        }

        // 3. Render Đường (Tích sẵn Mức đường cũ)
        rgSugar.removeAllViews();
        for (String sugar : apiData.getSugarLevels()) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(sugar.equals("NONE") ? "0%" : (sugar.equals("LESS") ? "50%" : "100%"));
            rgSugar.addView(rb);

            if (isEditMode && editItemData.getSugarLevel().equals(sugar)) { rb.setChecked(true); selectedSugar = sugar; }
            else if (!isEditMode && sugar.equals("NORMAL")) { rb.setChecked(true); selectedSugar = sugar; }

            rb.setOnClickListener(v -> { selectedSugar = sugar; updateUIPrice(); });
        }

        // 4. Render Đá (Tích sẵn Mức đá cũ)
        rgIce.removeAllViews();
        for (String ice : apiData.getIceLevels()) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(ice.equals("NONE") ? "Không đá" : (ice.equals("LESS") ? "Ít đá" : "Bình thường"));
            rgIce.addView(rb);

            if (isEditMode && editItemData.getIceLevel().equals(ice)) { rb.setChecked(true); selectedIce = ice; }
            else if (!isEditMode && ice.equals("NORMAL")) { rb.setChecked(true); selectedIce = ice; }

            rb.setOnClickListener(v -> { selectedIce = ice; updateUIPrice(); });
        }

        // 5. Render Topping (Tích sẵn những Topping cũ đã chọn)
        llToppings.removeAllViews();
        for (Topping t : apiData.getToppings()) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(t.getName() + " (+" + formatMoney(t.getPrice()) + ")");
            int cbId = View.generateViewId(); cb.setId(cbId);
            llToppings.addView(cb);
            toppingMap.put(cbId, t);

            // Kiểm tra xem tên topping này có nằm trong danh sách topping cũ của khách không
            boolean isOldChecked = false;
            if (isEditMode && editItemData.getToppingNames() != null) {
                isOldChecked = editItemData.getToppingNames().contains(t.getName());
            }
            cb.setChecked(isOldChecked);
            selectedToppings.put(t.getId(), isOldChecked);

            cb.setOnCheckedChangeListener((btn, isChecked) -> { selectedToppings.put(t.getId(), isChecked); updateUIPrice(); });
        }

        updateUIPrice(); // Tính tiền lần đầu ngay sau khi nạp nút
    }

    private void updateUIPrice() {
        tvQuantity.setText(String.valueOf(quantity));
        BigDecimal total = BigDecimal.ZERO;

        if (this.basePrice != null) total = total.add(this.basePrice);
        if (selectedSize != null && selectedSize.getPriceAdd() != null)
            total = total.add(selectedSize.getPriceAdd());

        for (Map.Entry<Integer, Topping> entry : toppingMap.entrySet()) {
            Topping t = entry.getValue();
            if (Boolean.TRUE.equals(selectedToppings.get(t.getId())) && t.getPrice() != null) {
                total = total.add(t.getPrice());
            }
        }

        BigDecimal finalTotal = total.multiply(BigDecimal.valueOf(quantity));
        btnAddToCart.setText("THÊM VÀO GIỎ HÀNG (" + formatMoney(finalTotal) + ")");
        btnAddToCart.setText(isEditMode ? "CẬP NHẬT" : "THÊM VÀO GIỎ");
    }

    private void callAddToCartApi() {
        // Validate: có sizes nhưng chưa chọn
        if (selectedSize == null && apiData != null
                && apiData.getSizes() != null && !apiData.getSizes().isEmpty()) {
            showToast("Vui lòng chọn kích thước");
            return;
        }

        List<Integer> toppingIds = new ArrayList<>();
        for(Map.Entry<Integer, Boolean> entry : selectedToppings.entrySet()){
            if(entry.getValue()) toppingIds.add(entry.getKey());
        }

        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);

        if (isEditMode) {
            // CHẾ ĐỘ SỬA -> Gọi API PUT
            UpdateCartRequest request = new UpdateCartRequest(
                    editItemData.getCartItemId(), // Truyền ID giỏ hàng để sửa
                    selectedSize != null ? selectedSize.getId() : 1,
                    selectedIce, selectedSugar, toppingIds, quantity
            );
            api.updateCartItem(request).enqueue(new Callback<BaseResponse<String>>() {
                @Override
                public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                    if(response.isSuccessful()) {
                        showToast("Cập nhật thành công!");
                        if(updateListener != null) updateListener.onCartUpdated(); // Báo cho Fragment biết để load lại
                        dismiss();
                    }
                }
                @Override public void onFailure(Call<BaseResponse<String>> call, Throwable t) {}
            });
        } else {
            // CHẾ ĐỘ THÊM MỚI -> Gọi API POST như cũ
            AddToCartRequest request = new AddToCartRequest(requestProductId, selectedSize != null ? selectedSize.getId() : 1, selectedIce, selectedSugar, toppingIds, quantity);
            api.addToCart(request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if(response.isSuccessful()) {
                    showToast("Thêm giỏ hàng thành công!");
                    dismiss();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                showToast("Lỗi mạng");
            }
        });
        }

//        // Dùng apiData.getProductId() lấy từ DB lúc nãy lên cho chuẩn 100%
//        AddToCartRequest request = new AddToCartRequest(
//                apiData.getProductId(),
//                selectedSize != null ? selectedSize.getId() : 1,
//                selectedIce,
//                selectedSugar,
//                toppingIds,
//                quantity
//        );
//
//        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);
//        api.addToCart(request).enqueue(new Callback<BaseResponse<String>>() {
//            @Override
//            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
//                if(response.isSuccessful()) {
//                    showToast("Thêm giỏ hàng thành công!");
//                    dismiss();
//                }
//            }
//            @Override
//            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
//                showToast("Lỗi mạng");
//            }
//        });
    }

    private android.widget.Toast currentToast;

    private void showToast(String msg) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0đ";
        return new DecimalFormat("#,###").format(amount.longValue()) + "đ";
    }
}