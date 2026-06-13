package com.example.myapplication.api;

import com.example.myapplication.model.AddressResponse;
import com.example.myapplication.model.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AddressApiService {
    // GET /api/address
    @GET("address")
    Call<BaseResponse<List<AddressResponse>>> getMyAddresses();
}