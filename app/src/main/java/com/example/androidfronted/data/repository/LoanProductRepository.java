package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.network.NetworkClient;
import com.example.androidfronted.util.TokenManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 贷款产品仓库
 * - GET /api/loan-products/user
 * - 自动携带 Token（由 AuthInterceptor 处理）
 */
public class LoanProductRepository {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/loan-products/user";
    private static final String TAG = "LoanProductRepo";

    private final OkHttpClient client;
    private final Gson gson;
    private final Context context;

    public LoanProductRepository(Context context) {
        this.context = context;
        this.client = NetworkClient.getOkHttpClient(context);
        this.gson = new Gson();
    }

    public void getLoanProducts(@NonNull AuthCallback<LoanProductResponse> callback) {
        // 调试：检查当前token
        String currentToken = new TokenManager(context).getToken();
        Log.d(TAG, "Current token for request: " + (currentToken != null ? "exists" : "null"));

        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Load loan products failed", e);
                postToMainThread(() -> callback.onError("网络错误"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        postToMainThread(() -> callback.onError("加载失败: " + response.code()));
                        return;
                    }
                    String responseBody = response.body().string();
                    LoanProductResponse res = gson.fromJson(responseBody, LoanProductResponse.class);
                    if (res != null && res.getCode() == 200 && res.getData() != null) {
                        postToMainThread(() -> callback.onSuccess(res));
                    } else {
                        String msg = (res != null && res.getMessage() != null) ? res.getMessage() : "未知错误";
                        postToMainThread(() -> callback.onError(msg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse loan products failed", e);
                    postToMainThread(() -> callback.onError("数据解析失败"));
                }
            }
        });
    }

    private void postToMainThread(Runnable runnable) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
    }

    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}