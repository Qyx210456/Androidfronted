package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.LoanOrderDetailResponse;
import com.example.androidfronted.data.model.LoanOrderResponse;
import com.example.androidfronted.data.model.PostponeResponse;
import com.example.androidfronted.data.model.RepaymentPlanResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LoanOrderApi {
    @GET("/api/orders/my")
    Call<LoanOrderResponse> getLoanOrders();

    @GET("/api/orders/{orderId}")
    Call<LoanOrderDetailResponse> getLoanOrderDetail(@Path("orderId") int orderId);

    @GET("/api/orders/{orderId}/repayment-plan")
    Call<RepaymentPlanResponse> getRepaymentPlan(@Path("orderId") int orderId);

    @POST("/api/orders/{orderId}/early-repay")
    Call<LoanOrderDetailResponse> earlyRepay(@Path("orderId") int orderId);

    @POST("/api/orders/{orderId}/postpone")
    Call<PostponeResponse> applyPostpone(@Path("orderId") int orderId);
}
