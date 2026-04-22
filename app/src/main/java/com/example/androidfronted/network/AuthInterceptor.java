package com.example.androidfronted.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.ui.LoginActivity;
import com.example.androidfronted.util.TokenManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 全局认证拦截器
 * - 为所有请求自动添加 Authorization: Bearer <token>
 * - 拦截 401 响应，尝试刷新 Token 后重试
 * - 刷新失败则清除 Token 并跳转登录页
 */
public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";
    private static final Object LOCK = new Object();
    private static volatile boolean isRefreshing = false;

    private final Context appContext;
    private final TokenManager tokenManager;
    private final Gson gson;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
        this.tokenManager = new TokenManager(appContext);
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();
        Log.d(TAG, "intercept: Request path = " + path);

        if (isAuthEndpoint(path)) {
            Log.d(TAG, "intercept: Auth endpoint detected, skipping token injection");
            return chain.proceed(original);
        }

        String token = tokenManager.getToken();
        Request.Builder builder = original.newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
            Log.d(TAG, "intercept: Added Authorization header with token");
        } else {
            Log.d(TAG, "intercept: No token available, request sent without Authorization header");
        }

        Request authorizedRequest = builder.build();
        Response response = chain.proceed(authorizedRequest);
        Log.d(TAG, "intercept: Response code = " + response.code() + " for path = " + path);

        if (response.code() == 401) {
            Log.d(TAG, "intercept: Received 401 Unauthorized, attempting token refresh");
            response.close();

            synchronized (LOCK) {
                String newToken = tokenManager.getToken();
                if (newToken != null && !newToken.equals(token)) {
                    Log.d(TAG, "intercept: Token already refreshed by another request, retrying");
                    return retryWithNewToken(chain, original, newToken);
                }

                if (isRefreshing) {
                    Log.d(TAG, "intercept: Another refresh is in progress, returning 401");
                    return buildUnauthorizedResponse(original);
                }

                isRefreshing = true;
            }

            try {
                boolean refreshed = refreshToken();
                synchronized (LOCK) {
                    isRefreshing = false;
                }

                if (refreshed) {
                    String refreshedToken = tokenManager.getToken();
                    if (refreshedToken != null) {
                        Log.d(TAG, "intercept: Token refresh successful, retrying request");
                        return retryWithNewToken(chain, original, refreshedToken);
                    }
                }

                Log.d(TAG, "intercept: Token refresh failed, handling refresh failure");
                handleRefreshFailed();
                return buildUnauthorizedResponse(original);

            } catch (Exception e) {
                Log.e(TAG, "intercept: Token refresh exception", e);
                synchronized (LOCK) {
                    isRefreshing = false;
                }
                handleRefreshFailed();
                return buildUnauthorizedResponse(original);
            }
        }

        return response;
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/refresh-token");
    }

    private Response retryWithNewToken(Chain chain, Request original, String newToken) throws IOException {
        Request newRequest = original.newBuilder()
                .header("Authorization", "Bearer " + newToken)
                .build();
        return chain.proceed(newRequest);
    }

    private boolean refreshToken() {
        String refreshToken = tokenManager.getRefreshToken();
        Log.d(TAG, "refreshToken() called, refreshToken: " + (refreshToken != null ? "exists" : "null"));
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.d(TAG, "No refresh token available");
            return false;
        }

        if (tokenManager.isRefreshTokenExpired()) {
            Log.d(TAG, "Refresh token has expired");
            return false;
        }

        Log.d(TAG, "Attempting to refresh token...");

        try {
            String json = gson.toJson(new RefreshTokenBody(refreshToken));
            Log.d(TAG, "Refresh token request body: " + json);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/auth/refresh-token")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "{}";
            Log.d(TAG, "Refresh token response code: " + response.code());
            Log.d(TAG, "Refresh token response body: " + responseBody);

            if (!response.isSuccessful()) {
                Log.e(TAG, "Refresh token request failed: " + response.code());
                return false;
            }

            LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);
            if (loginResponse != null && loginResponse.getData() != null) {
                String newToken = loginResponse.getData().getToken();
                String newRefreshToken = loginResponse.getData().getRefreshToken();
                Log.d(TAG, "New token: " + (newToken != null ? "exists" : "null"));
                Log.d(TAG, "New refreshToken: " + (newRefreshToken != null ? "exists" : "null"));

                if (newToken != null) {
                    tokenManager.saveTokens(newToken, newRefreshToken != null ? newRefreshToken : refreshToken);
                    Log.d(TAG, "Token refreshed successfully");
                    return true;
                }
            }

            Log.e(TAG, "Failed to parse refresh token response");
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error during token refresh", e);
            return false;
        }
    }

    private void handleRefreshFailed() {
        tokenManager.clearTokens();
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        });
    }

    private Response buildUnauthorizedResponse(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body(okhttp3.ResponseBody.create("{}", MediaType.get("application/json")))
                .build();
    }

    private static class RefreshTokenBody {
        @com.google.gson.annotations.SerializedName("refreshToken")
        private final String refreshToken;

        public RefreshTokenBody(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
