package com.example.myapplication.api;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.ProductDetailResponse;
import com.example.myapplication.model.ProductResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ProductApiService {

    // GET /api/products  → tất cả sản phẩm (không lọc)
    @GET("products")
    Call<BaseResponse<List<ProductResponse>>> getAllProducts();

    // GET /api/products/best-sellers
    @GET("products/best-sellers")
    Call<BaseResponse<List<ProductResponse>>> getBestSellers();

    // GET /api/products/search?keyword=...&categoryId=...&sortBy=...
    @GET("products/search")
    Call<BaseResponse<List<ProductResponse>>> searchProducts(
            @QueryMap Map<String, String> filters);

    // GET /api/products/recommendations
    @GET("products/recommendations")
    Call<BaseResponse<List<ProductResponse>>> getRecommendations();

    // GET /api/products/{id}  → ProductDetailResponse (co ca reviews)
    @GET("products/{id}")
    Call<BaseResponse<ProductDetailResponse>> getProductDetail(@Path("id") int id);

    // GET /api/products/{id}/options → Size, Topping, Duong, Da
    @GET("products/{id}/options")
    Call<BaseResponse<Object>> getProductOptions(@Path("id") int id);
}