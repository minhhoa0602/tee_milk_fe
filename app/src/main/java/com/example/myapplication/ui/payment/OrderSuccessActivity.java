package com.example.myapplication.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.api.OrderApi;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.order.OrderResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderSuccessActivity extends AppCompatActivity {

    private LinearLayout layoutQrCode;
    private ImageView ivQrCode;
    private TextView tvSuccessTitle, tvSuccessMessage;
    private Button btnBackHome;

    private int orderId;
    private boolean isPolling = false;
    private Handler pollingHandler = new Handler(Looper.getMainLooper());

    // Runnable: Cỗ máy chạy lặp đi lặp lại
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            checkPaymentStatusFromBackend();
            if (isPolling) {
                pollingHandler.postDelayed(this, 3000); // Hẹn giờ 3 giây sau chạy tiếp
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        layoutQrCode = findViewById(R.id.layoutQrCode);
        ivQrCode = findViewById(R.id.ivQrCode);
        tvSuccessTitle = findViewById(R.id.tvSuccessTitle);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        btnBackHome = findViewById(R.id.btnBackHome);

        // Hứng dữ liệu từ màn Checkout
        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        String paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");
        String qrUrl = getIntent().getStringExtra("QR_URL");

        if ("MOMO".equals(paymentMethod) || "BANK".equals(paymentMethod)) {
            // LUỒNG 1: CHUYỂN KHOẢN (Hiển thị QR & Chờ đợi)
            layoutQrCode.setVisibility(View.VISIBLE);
            tvSuccessTitle.setText("Chờ thanh toán...");
            tvSuccessMessage.setText("Sử dụng ứng dụng Ngân hàng hoặc MoMo quét mã dưới đây. Màn hình sẽ tự động chuyển khi nhận được tiền.");
            btnBackHome.setVisibility(View.GONE);

            // Tải ảnh VietQR
            Glide.with(this).load(qrUrl).into(ivQrCode);

            // 🚀 BẬT CHẾ ĐỘ CHẠY NGẦM HỎI THĂM
            isPolling = true;
            pollingHandler.postDelayed(pollingRunnable, 3000);

        } else {
            // LUỒNG 2: TIỀN MẶT (Xong luôn)
            layoutQrCode.setVisibility(View.GONE);
            tvSuccessTitle.setText("Đặt hàng thành công!");
            tvSuccessMessage.setText("Cảm ơn bạn. Đơn hàng đang được chuẩn bị và sẽ giao đến sớm nhất!");
        }

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Xóa sạch lịch sử các màn hình Checkout để khi ấn Back nó ko lùi lại
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkPaymentStatusFromBackend() {
        OrderApi api = RetrofitClient.getInstance(this).create(OrderApi.class);
        api.getOrderById(orderId).enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    OrderResponse orderInfo = response.body().getData();

                    // 🔥 PHÉP MÀU: Nếu SePay đã bắn Webhook và đổi status thành PAID
                    if ("PAID".equals(orderInfo.getPaymentStatus())) {
                        isPolling = false; // Lập tức tắt máy chạy ngầm đi

                        // HIỆU ỨNG GIAO DIỆN CHÚC MỪNG
                        layoutQrCode.setVisibility(View.GONE);
                        tvSuccessTitle.setText("🎉 THANH TOÁN THÀNH CÔNG!");
                        tvSuccessTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        tvSuccessMessage.setText("Hệ thống đã nhận được tiền của bạn. Tee Milk đang pha chế đồ uống ngay lập tức!");
                        btnBackHome.setVisibility(View.VISIBLE); // Bật nút để khách thoát
                    }
                }
            }
            @Override public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cực kỳ quan trọng: Khách thoát app ngang phải tắt máy chạy ngầm đi
        isPolling = false;
        pollingHandler.removeCallbacks(pollingRunnable);
    }
}