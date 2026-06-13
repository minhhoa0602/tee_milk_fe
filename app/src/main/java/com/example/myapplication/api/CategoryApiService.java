package com.example.myapplication.api;

import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryApiService {
    // GET /api/categories
    @GET("categories")
    Call<BaseResponse<List<CategoryResponse>>> getCategories();
}