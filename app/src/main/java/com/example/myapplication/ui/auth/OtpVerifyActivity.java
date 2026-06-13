//package com.example.myapplication.ui.auth;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.myapplication.R;
//import com.example.myapplication.api.ApiService;
//import com.example.myapplication.api.RetrofitClient;
//import com.example.myapplication.model.BaseResponse;
//import com.example.myapplication.model.VerifyRequest;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class OtpVerifyActivity extends AppCompatActivity {
//
//    public static final String EXTRA_EMAIL = "EMAIL";
//
//    private EditText etOtp;
//    private Button btnVerify;
//    private TextView tvEmail;
//    private String email;
//    private ApiService apiService;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_otp_verify);
//
//        email = getIntent().getStringExtra(EXTRA_EMAIL);
//
//        etOtp = findViewById(R.id.etOtp);
//        btnVerify = findViewById(R.id.btnVerify);
//        tvEmail = findViewById(R.id.tvEmail);
//
//        if (tvEmail != null && email != null) tvEmail.setText(email);
//
//        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
//
//        btnVerify.setOnClickListener(v -> verifyOtp());
//    }
//
//    private void verifyOtp() {
//        String otp = etOtp.getText().toString().trim();
//        if (otp.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        apiService.verifyOtp(new VerifyRequest(email, otp)).enqueue(new Callback<BaseResponse<Void>>() {
//            @Override
//            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(OtpVerifyActivity.this, "Xác thực thành công! Mời đăng nhập", Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent(OtpVerifyActivity.this, LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    String msg = (response.body() != null && response.body().getMessage() != null)
//                            ? response.body().getMessage() : "OTP không hợp lệ";
//                    Toast.makeText(OtpVerifyActivity.this, msg, Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
//                Toast.makeText(OtpVerifyActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}



package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.VerifyRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerifyActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "EMAIL";

    private EditText etOtp;
    private Button btnVerify;
    private TextView tvEmail;
    private String email;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        // Nhận Email an toàn
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_EMAIL)) {
            email = intent.getStringExtra(EXTRA_EMAIL);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy Email để xác thực", Toast.LENGTH_LONG).show();
            finish(); // Đóng về màn hình trước nếu không có email
            return;
        }

        initViews();
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void initViews() {
        try {
            etOtp = findViewById(R.id.etOtp);
            btnVerify = findViewById(R.id.btnVerify);
            tvEmail = findViewById(R.id.tvEmail);

            if (tvEmail != null) {
                tvEmail.setText("Mã OTP đã gửi đến: " + email);
            }
        } catch (Exception e) {
            Log.e("OtpVerify", "Lỗi init view: " + e.getMessage());
        }
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem VerifyRequest có đúng thứ số (email, otp) không
        apiService.verifyOtp(new VerifyRequest(email, otp))
                // QUAN TRỌNG: ĐỔI Void -> Object
                .enqueue(new Callback<BaseResponse<Object>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {

                        // Thêm log để debug
                        Log.d("OtpDebug", "Verify Code: " + response.code());

                        if (response.isSuccessful()) {
                            Toast.makeText(OtpVerifyActivity.this, "Xác thực thành công! Mời đăng nhập", Toast.LENGTH_LONG).show();

                            // Chuyển về màn hình đăng nhập
                            Intent intent = new Intent(OtpVerifyActivity.this, LoginActivity.class);
                            // Cờ này giúp xóa hết các màn hình đăng ký/OTP đứng trước đó, user bấm back sẽ không quay lại được
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            finish(); // Đóng màn hình OTP
                        } else {
                            String msg = "OTP không hợp lệ hoặc đã hết hạn";
                            try {
                                if (response.errorBody() != null) {
                                    msg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(OtpVerifyActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                        Toast.makeText(OtpVerifyActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("OtpDebug", "Error: " + t.getMessage());
                    }
                });
    }
}