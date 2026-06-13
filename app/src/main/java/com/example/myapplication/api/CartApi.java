package com.example.myapplication.api;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.cart.AddToCartRequest;
import com.example.myapplication.model.cart.CartItem;
import com.example.myapplication.model.cart.UpdateCartRequest;
import com.example.myapplication.model.order.ProductOptionsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartApi {

    // 1. Lấy tùy chọn sản phẩm
    @GET("products/{id}/options")
    Call<BaseResponse<ProductOptionsResponse>> getProductOptions(@Path("id") int productId);

    // 2. Thêm vào giỏ hàng
    @POST("cart/add")
    Call<BaseResponse<String>> addToCart(@Body AddToCartRequest request);


    // 1. Lấy danh sách giỏ hàng
    @GET("cart")
    Call<BaseResponse<List<CartItem>>> getCartItems();

    // 2. Cập nhật món trong giỏ
    @PUT("cart/update")
    Call<BaseResponse<String>> updateCartItem(@Body UpdateCartRequest request);

    // 3. Xóa món (truyền ID) hoặc Xóa tất cả (truyền null)
    @DELETE("cart/{id}")
    Call<BaseResponse<String>> deleteCartItem(@Path("id") Integer id);

    @DELETE("cart")
    Call<BaseResponse<String>> deleteAllCartItems();
}
