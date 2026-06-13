package com.example.myapplication.api;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.order.OrderRequest;
import com.example.myapplication.model.order.OrderResponse;
import com.example.myapplication.model.order.ProductOptionsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApi {
//    @GET("products/{id}/options")
//    Call<BaseResponse<ProductOptionsResponse>> getProductOptions(
//            @Path("id") Integer id
//    );

    @POST("orders")
    Call<BaseResponse<OrderResponse>> placeOrder(@Body OrderRequest request);

    @GET("orders/{id}")
    Call<BaseResponse<OrderResponse>> getOrderById(@Path("id") int orderId);
}
