package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.UserInfoResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserApi {
    /**
     * 获取用户信息
     */
    @GET("/api/user/info")
    Call<UserInfoResponse> getUserInfo();

    /**
     * 更新用户信息
     */
    @Multipart
    @POST("/api/user/update")
    Call<UserInfoResponse> updateUserInfo(
            @Part("username") RequestBody username,
            @Part MultipartBody.Part avatar
    );
}