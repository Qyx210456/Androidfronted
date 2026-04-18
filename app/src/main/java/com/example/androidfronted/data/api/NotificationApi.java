package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.NotificationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("/api/notifications/my")
    Call<NotificationResponse> getMyNotifications(@Query("limit") int limit);

    @PATCH("/api/notifications/{notificationId}/read")
    Call<com.example.androidfronted.data.model.NotificationResponse> markAsRead(@Path("notificationId") int notificationId);
}
