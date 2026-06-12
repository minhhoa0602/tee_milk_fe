package com.example.myapplication.api;

import com.example.myapplication.model.Address;
import com.example.myapplication.model.AddressRequest;
import com.example.myapplication.model.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AddressApi {
    @GET("address")
    Call<BaseResponse<List<Address>>> getAddresses();

    @POST("address")
    Call<BaseResponse<Address>> addAddress(@Body AddressRequest request);
}
