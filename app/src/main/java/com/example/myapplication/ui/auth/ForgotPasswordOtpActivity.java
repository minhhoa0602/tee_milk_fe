package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.ForgotPasswordVerifyRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordOtpActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "EMAIL";

    private EditText etOtp;
    private Button btnVerify;
    private TextView tvEmail;
    private String email;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        email = getIntent().getStringExtra(EXTRA_EMAIL);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        tvEmail = findViewById(R.id.tvEmail);

        if (tvEmail != null && email != null) tvEmail.setText(email);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.verifyForgotPasswordOtp(new ForgotPasswordVerifyRequest(email, otp))
                .enqueue(new Callback<BaseResponse<String>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String resetToken = response.body().getData();
                            Intent intent = new Intent(ForgotPasswordOtpActivity.this, ResetPasswordActivity.class);
                            intent.putExtra(ResetPasswordActivity.EXTRA_TOKEN, resetToken != null ? resetToken : "");
                            startActivity(intent);
                            finish();
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "OTP không hợp lệ";
                            Toast.makeText(ForgotPasswordOtpActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                        Toast.makeText(ForgotPasswordOtpActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
