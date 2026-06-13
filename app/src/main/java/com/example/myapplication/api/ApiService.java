package com.example.myapplication.api;

import com.example.myapplication.model.Address;
import com.example.myapplication.model.AddressRequest;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.CartRequest;
import com.example.myapplication.model.ForgotPasswordRequest;
import com.example.myapplication.model.ForgotPasswordVerifyRequest;
import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.ProductDetail;
import com.example.myapplication.model.ProductOptions;
import com.example.myapplication.model.RegisterRequest;
import com.example.myapplication.model.ResetPasswordWithTokenRequest;
import com.example.myapplication.model.ReviewRequest;
import com.example.myapplication.model.UpdateProfileRequest;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.model.VerifyRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

//    @POST("auth/register")
//    Call<BaseResponse<Void>> register(@Body RegisterRequest request);

    @POST("auth/register")
    Call<BaseResponse<Object>> register(@Body RegisterRequest request);

    @POST("auth/verify")
    Call<BaseResponse<Object>> verifyOtp(@Body VerifyRequest request);

    @POST("auth/forgot-password")
    Call<BaseResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/forgot-password/verify")
    Call<BaseResponse<String>> verifyForgotPasswordOtp(@Body ForgotPasswordVerifyRequest request);

    @POST("auth/forgot-password/reset")
    Call<BaseResponse<Void>> resetPassword(@Body ResetPasswordWithTokenRequest request);

    @POST("auth/logout")
    Call<BaseResponse<Void>> logout();

    @GET("user/profile")
    Call<BaseResponse<UserProfile>> getProfile();

    @PUT("user/profile")
    Call<BaseResponse<UserProfile>> updateProfile(@Body UpdateProfileRequest request);

    @GET("address")
    Call<BaseResponse<List<Address>>> getAddresses();

    @POST("address")
    Call<BaseResponse<Address>> addAddress(@Body AddressRequest request);

    @GET("orders")
    Call<BaseResponse<List<Order>>> getOrders(@Query("status") String status);

    @POST("orders/{id}/reorder")
    Call<BaseResponse<Void>> reorder(@Path("id") int orderId);

    @POST("reviews")
    Call<BaseResponse<Void>> postReview(@Body ReviewRequest reviewRequest);

    @GET("products/{id}")
    Call<BaseResponse<ProductDetail>> getProductDetail(@Path("id") int productId);

    @GET("products/{id}/options")
    Call<BaseResponse<ProductOptions>> getProductOptions(@Path("id") int productId);

    @POST("cart/add")
    Call<BaseResponse<Void>> addToCart(@Body CartRequest request);
}
