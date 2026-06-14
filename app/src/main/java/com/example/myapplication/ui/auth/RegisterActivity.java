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
import com.example.myapplication.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        btnRegister.setOnClickListener(v -> register());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.register(new RegisterRequest(email, fullName, password, confirmPassword))
                .enqueue(new Callback<BaseResponse<Object>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {
                        Log.d("RegisterDebug", "Code: " + response.code() + ", Message: " + response.message());

                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng xác thực OTP", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, OtpVerifyActivity.class);
                            intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = "Đăng ký thất bại";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterDebug", "Error: " + t.getMessage());
                    }
                });
    }
}
