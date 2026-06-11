package com.example.myapplication.api;

import com.example.myapplication.model.Address;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.RegisterRequest;
import com.example.myapplication.model.ReviewRequest;
import com.example.myapplication.model.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")  // điều chỉnh endpoint cho đúng backend của bạn
    Call<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<BaseResponse<UserProfile>> register(@Body RegisterRequest request);

    @GET("user/profile")
    Call<BaseResponse<UserProfile>> getProfile();

    @PUT("user/profile")
    Call<BaseResponse<UserProfile>> updateProfile(@Body UserProfile profile);

    @GET("address")
    Call<BaseResponse<List<Address>>> getAddresses();

    @POST("address")
    Call<BaseResponse<Address>> addAddress(@Body Address address);

    @GET("orders")
    Call<BaseResponse<List<Order>>> getOrders(@Query("status") String status);

    @POST("orders/{orderId}/reorder")
    Call<BaseResponse<Void>> reorder(@Path("orderId") int orderId);

    @POST("reviews")
    Call<BaseResponse<Void>> postReview(@Body ReviewRequest reviewRequest);

    @POST("auth/logout")
    Call<BaseResponse<Void>> logout();
}
