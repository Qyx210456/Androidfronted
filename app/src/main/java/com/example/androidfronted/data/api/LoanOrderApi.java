package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.LoanOrderDetailResponse;
import com.example.androidfronted.data.model.LoanOrderResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LoanOrderApi {
    @GET("/api/orders/my")
    Call<LoanOrderResponse> getLoanOrders();

    @GET("/api/orders/{orderId}")
    Call<LoanOrderDetailResponse> getLoanOrderDetail(@Path("orderId") int orderId);
}
