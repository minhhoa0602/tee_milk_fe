package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initApi();

        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> goToRegister());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void initApi() {
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        tokenManager = new TokenManager(this);
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        Log.d("LOGIN_DEBUG", "Đang gửi yêu cầu đăng nhập cho: " + email);

        apiService.login(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginResponse>> call, Response<BaseResponse<LoginResponse>> response) {
                Log.d("LOGIN_DEBUG", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginResponse> baseResponse = response.body();
                    if (baseResponse.getData() != null) {
                        LoginResponse loginData = baseResponse.getData();
                        tokenManager.saveToken(loginData.getToken());

                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        Log.d("LOGIN_DEBUG", "Token: " + loginData.getToken());

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = baseResponse.getMessage() != null ? baseResponse.getMessage() : "Lỗi dữ liệu từ server";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN_DEBUG", "Đăng nhập thất bại: " + msg);
                    }
                } else {
                    String errorMsg = "Email hoặc mật khẩu không đúng";
                    if (response.code() == 500) {
                        errorMsg = "Lỗi Server (500) - Kiểm tra kết nối Database của Backend";
                    } else if (response.code() == 404) {
                        errorMsg = "Không tìm thấy Endpoint (404)";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN_DEBUG", "Lỗi HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                Log.e("LOGIN_DEBUG", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage() + ". Kiểm tra Server!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}