package com.example.androidfronted.network;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.androidfronted.ui.LoginActivity;
import com.example.androidfronted.util.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 全局认证拦截器
 * - 为所有请求自动添加 Authorization: Bearer <token>
 * - 拦截 401 响应，清除 Token 并跳转登录页
 */
public class AuthInterceptor implements Interceptor {

    private final Context appContext;
    private final TokenManager tokenManager;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
        this.tokenManager = new TokenManager(appContext);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        // 排除认证相关接口（登录/注册不需要 Token）
        String path = original.url().encodedPath();
        if (path.startsWith("/api/auth/")) {
            return chain.proceed(original);
        }

        // 附加 Token
        String token = tokenManager.getToken();
        Request.Builder builder = original.newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request authorizedRequest = builder.build();
        Response response = chain.proceed(authorizedRequest);

        // 处理 401
        if (response.code() == 401) {
            new android.os.Handler(appContext.getMainLooper()).post(() -> {
                tokenManager.clearToken();
                Intent intent = new Intent(appContext, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
            });
        }

        return response;
    }
}
