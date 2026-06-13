package com.example.myapplication.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.api.CartApi;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.cart.CartItem;
import com.example.myapplication.model.cart.UpdateCartRequest;
import com.example.myapplication.ui.order.OptionsBottomSheet;
import com.example.myapplication.ui.payment.CheckoutActivity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private RecyclerView rvCartItems;
    private CartAdapter adapter;
    private List<CartItem> listItems = new ArrayList<>();

    private CheckBox cbSelectAll;
    private ImageView btnDeleteAll;
    private TextView tvSummaryCount, tvTotalPrice;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Ánh xạ các trường UI ở file XML khung ngoài
        rvCartItems = view.findViewById(R.id.rvCartItems);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        btnDeleteAll = view.findViewById(R.id.btnDeleteAll);
        tvSummaryCount = view.findViewById(R.id.tvSummaryCount);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Tải danh sách giỏ hàng từ Spring Boot lên
        loadCartData();

        // 2. Xử lý nút "Chọn tất cả" công tắc tổng
        cbSelectAll.setOnClickListener(v -> {
            boolean checked = cbSelectAll.isChecked();
            for (CartItem item : listItems) {
                item.setChecked(checked);
            }
            adapter.notifyDataSetChanged();
            onItemCheckChanged(); // Tính toán lại tiền
        });

        // 3. Xử lý nút xóa sạch sành sanh giỏ hàng (API DELETE không truyền ID)
        btnDeleteAll.setOnClickListener(v -> clearAllCartItemsApi());

        btnCheckout.setOnClickListener(v -> payment());

        return view;
    }

    private void loadCartData() {
        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);
        api.getCartItems().enqueue(new Callback<BaseResponse<List<CartItem>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<CartItem>>> call, Response<BaseResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listItems = response.body().getData();

                    // Gán adapter vào RecyclerView
                    adapter = new CartAdapter(getContext(), listItems, CartFragment.this);
                    rvCartItems.setAdapter(adapter);

                    onItemCheckChanged(); // Tính tiền mặc định ban đầu
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CartItem>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi lấy dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- XỬ LÝ SỰ KIỆN CALLBACK TỪ ADAPTER BẮN RA ---

    @Override
    public void onQuantityChanged(CartItem item, int newQty) {
        // Gom dữ liệu hiện tại để gửi lệnh cập nhật số lượng lên BE (Dùng API PUT)
        // Lưu ý: Các ID tùy chọn như sizeId/toppingIds ta tạm truyền giá trị cứng test hoặc lấy từ cấu trúc DB của bạn
        List<Integer> emptyToppings = new ArrayList<>(); // Gài tạm list trống hoặc đổ ID từ item vào nếu BE yêu cầu
        UpdateCartRequest request = new UpdateCartRequest(item.getCartItemId(), 3, item.getIceLevel(), item.getSugarLevel(), emptyToppings, newQty);

        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);
        api.updateCartItem(request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 🔥 [DÁN ĐỀ PHẦN CODE NÀY VÀO ĐÂY] 🔥

                    // 1. TÍNH TOÁN LẠI GIÁ TIỀN CỦA LY TRÀ SỮA NÀY LÚC NÀY
                    BigDecimal unitPrice = item.getPrice(); // Giá của 1 ly
                    BigDecimal finalTotalItemPrice = unitPrice.multiply(BigDecimal.valueOf(newQty)); // Nhân với số lượng mới

                    // 2. GÁN LẠI VÀO OBJECT TRÀ SỮA ĐỂ CẬP NHẬT DỮ LIỆU LOCAL
                    item.setQuantity(newQty); // Sửa số lượng
                    item.setTotalPrice(finalTotalItemPrice); // *** FIX LỖI 2: Sửa tổng giá tiền của 1 ly *** (Bạn nhớ tạo setter này trong CartItem nhé)

                    // 3. THÔNG BÁO GIAO DIỆN VẼ LẠI
                    adapter.notifyDataSetChanged(); // Vẽ lại ly trà sữa với giá tiền mới (Fix lỗi 2)
                    onItemCheckChanged(); // Tính toán lại tổng tiền dưới đáy của toàn giỏ hàng (Fix lỗi 3)
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Cập nhật số lượng thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemDeleted(int cartItemId, int position) {
        // Gọi API DELETE /api/cart/{id} (Xóa 1 món cố định)
        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);
        api.deleteCartItem(cartItemId).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    listItems.remove(position); // Bóc món đó ra khỏi mảng cục bộ
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, listItems.size());
                    onItemCheckChanged(); // Tính lại tiền tổng
                    Toast.makeText(getContext(), "Đã xóa sản phẩm khỏi giỏ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Xóa món thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemCheckChanged() {
        // 🔥 TRÁI TIM TÍNH TIỀN CHUẨN UX SHOPEE
        BigDecimal totalBill = BigDecimal.ZERO;
        int checkedCount = 0;

        for (CartItem item : listItems) {
            if (item.isChecked()) {
                totalBill = totalBill.add(item.getTotalPrice());
                checkedCount++;
            }
        }

        // Đổ số liệu ra màn hình
        tvSummaryCount.setText("Tạm tính (" + checkedCount + " sản phẩm)");
        tvTotalPrice.setText(new DecimalFormat("#,###đ").format(totalBill));

        // Kiểm tra xem có đang tích đủ tất cả các món không để bật/tắt ô "Chọn tất cả" ngoài rìa
        cbSelectAll.setChecked(checkedCount == listItems.size() && listItems.size() > 0);
    }

    @Override
    public void onItemEditRequested(CartItem item) {
        // Dùng Constructor số 2: Truyền productId và nguyên cái cục item cũ sang
        OptionsBottomSheet bottomSheet = new OptionsBottomSheet(item.getProductId(), item);

        // Cài đặt tai nghe để khi Popup báo "Lưu xong rồi", Giỏ hàng sẽ tự tải lại dữ liệu mới từ mạng về
        bottomSheet.setUpdateListener(new OptionsBottomSheet.BottomSheetListener() {
            @Override
            public void onCartUpdated() {
                loadCartData(); // Hàm gọi API GET danh sách của bạn
            }
        });

        bottomSheet.show(getChildFragmentManager(), "OptionsBottomSheetEdit");
    }

    private void clearAllCartItemsApi() {
        // Gọi API DELETE /api/cart (Hàm xóa sạch sành sanh chúng ta vừa bổ sung ở Backend)
        CartApi api = RetrofitClient.getInstance(getContext()).create(CartApi.class);
        api.deleteAllCartItems().enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    listItems.clear(); // Làm rỗng danh sách
                    adapter.notifyDataSetChanged();
                    onItemCheckChanged(); // Tiền về 0đ
                    Toast.makeText(getContext(), "Đã dọn sạch giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi dọn giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void payment() {
        ArrayList<Integer> selectedIds = new ArrayList<>();
        for (CartItem item : listItems) {
            if (item.isChecked()) {
                selectedIds.add(item.getCartItemId());
            }
        }

        // Rào lỗi: Khách chưa chọn món nào mà đòi thanh toán
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 món để thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gói ghém dữ liệu lên xe và chuyển sang màn CheckoutActivity
        Intent intent = new Intent(getContext(), CheckoutActivity.class);
        intent.putIntegerArrayListExtra("CART_IDS", selectedIds);
        // Truyền theo tổng tiền để màn kia hiển thị luôn cho nhanh
        intent.putExtra("TOTAL_AMOUNT", tvTotalPrice.getText().toString());
        startActivity(intent);
    }
}