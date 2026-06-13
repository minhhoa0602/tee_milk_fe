package com.example.myapplication.ui.payment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.api.AddressApi;
import com.example.myapplication.api.OrderApi;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.Address;
import com.example.myapplication.model.AddressRequest;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.cart.CartItem;
import com.example.myapplication.model.order.OrderRequest;
import com.example.myapplication.model.order.OrderResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etName, etPhone, etNote;
    private RadioGroup rgPayment;
    private TextView tvTotalCheckout, tvSelectedAddress;
    private LinearLayout layoutAddressPicker;
    private Button btnPlaceOrder;

    private ArrayList<Integer> selectedCartItemIds;

    // Quản lý địa chỉ
    private List<Address> addressList = new ArrayList<>();
    private int selectedAddressId = -1; // -1 nghĩa là chưa chọn địa chỉ nào
    private androidx.recyclerview.widget.RecyclerView rvOrderItems;
    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> displayItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etNote = findViewById(R.id.etNote);
        rgPayment = findViewById(R.id.rgPayment);
        tvTotalCheckout = findViewById(R.id.tvTotalCheckout);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        layoutAddressPicker = findViewById(R.id.layoutAddressPicker);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        selectedCartItemIds = getIntent().getIntegerArrayListExtra("CART_IDS");
        tvTotalCheckout.setText(getIntent().getStringExtra("TOTAL_AMOUNT"));

        // 1. Tải danh sách địa chỉ từ API
        loadAddresses();

        // 2. Bắt sự kiện khi khách bấm vào ô chọn địa chỉ
        layoutAddressPicker.setOnClickListener(v -> showAddressSelectionDialog());

        loadSelectedProducts();

        // 3. Xử lý nút thanh toán
        btnPlaceOrder.setOnClickListener(v -> submitOrder());
    }

    private void loadAddresses() {
        AddressApi api = RetrofitClient.getInstance(this).create(AddressApi.class);
        api.getAddresses().enqueue(new Callback<BaseResponse<List<Address>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Address>>> call, Response<BaseResponse<List<Address>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addressList = response.body().getData();

                    if (addressList == null || addressList.isEmpty()) {
                        tvSelectedAddress.setText("Chưa có địa chỉ. Bấm để thêm mới!");
                        selectedAddressId = -1;
                        return;
                    }

                    // Tự động tìm địa chỉ mặc định (isDefault = true)
                    boolean foundDefault = false;
                    for (Address addr : addressList) {
                        if (addr.isDefault()) {
                            tvSelectedAddress.setText(addr.getAddressLine());
                            selectedAddressId = addr.getId();
                            foundDefault = true;
                            break;
                        }
                    }

                    // Nếu không có cái nào mặc định, tự động lấy cái đầu tiên
                    if (!foundDefault) {
                        tvSelectedAddress.setText(addressList.get(0).getAddressLine());
                        selectedAddressId = addressList.get(0).getId();
                    }
                }
            }
            @Override public void onFailure(Call<BaseResponse<List<Address>>> call, Throwable t) {
                tvSelectedAddress.setText("Lỗi tải địa chỉ mạng");
            }
        });
    }

    // 🚀 HIỆN POPUP DROPDOWN ĐỂ CHỌN HOẶC THÊM ĐỊA CHỈ
    private void showAddressSelectionDialog() {
        // Tạo một mảng String để nhét vào Dialog
        String[] options = new String[addressList.size() + 1];

        for (int i = 0; i < addressList.size(); i++) {
            Address addr = addressList.get(i);
            options[i] = addr.getAddressLine() + (addr.isDefault() ? " (Mặc định)" : "");
        }

        // Món chốt cuối cùng là nút Thêm địa chỉ mới
        int addIndex = addressList.size();
        options[addIndex] = "➕ Thêm địa chỉ mới...";

        new AlertDialog.Builder(this)
                .setTitle("Chọn địa chỉ giao hàng")
                .setItems(options, (dialog, which) -> {
                    if (which == addIndex) {
                        // Nếu bấm vào dòng cuối cùng -> Hiện Popup nhập text thêm mới
                        showAddNewAddressDialog();
                    } else {
                        // Nếu bấm vào địa chỉ cũ -> Đổi chữ trên giao diện và lưu ID lại
                        Address pickedAddr = addressList.get(which);
                        tvSelectedAddress.setText(pickedAddr.getAddressLine());
                        selectedAddressId = pickedAddr.getId();
                    }
                })
                .show();
    }

    // 🚀 HIỆN POPUP NHẬP ĐỊA CHỈ MỚI RỒI GỌI API POST
    private void showAddNewAddressDialog() {
        EditText input = new EditText(this);
        input.setHint("Nhập địa chỉ nhà, tên đường...");
        input.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("Thêm địa chỉ mới")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newAddressText = input.getText().toString().trim();
                    if (!newAddressText.isEmpty()) {
                        addNewAddressApi(newAddressText);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadSelectedProducts() {
        // Gọi lại API lấy danh sách Giỏ hàng
        com.example.myapplication.api.CartApi api = RetrofitClient.getInstance(this).create(com.example.myapplication.api.CartApi.class);
        api.getCartItems().enqueue(new Callback<BaseResponse<List<CartItem>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<CartItem>>> call, Response<BaseResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<CartItem> allCartItems = response.body().getData();

                    // Lọc ra NHỮNG MÓN KHÁCH ĐÃ CHỌN (So sánh ID món ăn với mảng ID truyền sang)
                    displayItemList.clear();
                    if (selectedCartItemIds != null) {
                        for (CartItem item : allCartItems) {
                            if (selectedCartItemIds.contains(item.getCartItemId())) {
                                displayItemList.add(item);
                            }
                        }
                    }

                    // Đổ dữ liệu vào Adapter
                    checkoutAdapter = new CheckoutAdapter(CheckoutActivity.this, displayItemList);
                    rvOrderItems.setAdapter(checkoutAdapter);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CartItem>>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Không thể tải danh sách sản phẩm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewAddressApi(String addressLine) {
        // Gọi API thêm địa chỉ (Cho mặc định là false, hoặc true tùy bạn)
        AddressRequest request = new AddressRequest(addressLine, true);

        AddressApi api = RetrofitClient.getInstance(this).create(AddressApi.class);
        api.addAddress(request).enqueue(new Callback<BaseResponse<Address>>() {
            @Override
            public void onResponse(Call<BaseResponse<Address>> call, Response<BaseResponse<Address>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CheckoutActivity.this, "Đã thêm địa chỉ!", Toast.LENGTH_SHORT).show();
                    // Load lại danh sách địa chỉ từ Server
                    loadAddresses();
                }
            }
            @Override public void onFailure(Call<BaseResponse<Address>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi thêm địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và SĐT người nhận!", Toast.LENGTH_SHORT).show();
            return;
        }

        // BẮT LỖI CHƯA CHỌN ĐỊA CHỈ
        if (selectedAddressId == -1) {
            Toast.makeText(this, "Vui lòng thêm hoặc chọn địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rgPayment.getCheckedRadioButtonId() == R.id.rbMomo ? "MOMO" : "CASH";

        // 👉 CHÈN ID ĐỊA CHỈ THỰC TẾ VÀO ĐÂY (Thay cho số 4 cứng lúc trước)
        OrderRequest request = new OrderRequest(name, phone, selectedAddressId, selectedCartItemIds, paymentMethod, note);

        OrderApi api = RetrofitClient.getInstance(this).create(OrderApi.class);
        api.placeOrder(request).enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    OrderResponse orderRes = response.body().getData();

                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("ORDER_ID", orderRes.getOrderId());
                    intent.putExtra("PAYMENT_METHOD", orderRes.getPaymentMethod());
                    intent.putExtra("QR_URL", orderRes.getQrCodeUrl());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Đơn hàng thất bại, kiểm tra lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}