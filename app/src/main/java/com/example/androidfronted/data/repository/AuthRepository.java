package com.example.androidfronted.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.network.NetworkClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 认证仓库（Authentication Repository）
 * <p>
 * 负责封装用户登录与注册的网络请求逻辑。
 * 使用 OkHttp 发起 HTTP 请求，Gson 解析 JSON 响应。
 * 所有回调均在主线程（UI 线程）中执行，便于直接更新界面。
 * </p>
 */
public class AuthRepository {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/auth";
    private static final String TAG = "AuthRepository";

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";

    private final Context appContext;
    private final OkHttpClient client;
    private final Gson gson;

    public AuthRepository(Context context) {
        // 保存 ApplicationContext 用于 SharedPreferences
        this.appContext = context.getApplicationContext();
        this.client = NetworkClient.getOkHttpClient(context);
        this.gson = new Gson();
    }

    /**
     * 执行用户登录操作
     * <p>
     * 将 {@link LoginRequest} 转换为 JSON 并发送 POST 请求到 /api/auth/login。
     * 成功时解析响应并保存 Token；失败时返回错误信息。
     * </p>
     *
     * @param request  登录请求体，包含手机号和密码
     * @param callback 异步回调接口，用于通知调用者操作结果（成功或失败）
     */
    public void login(@NonNull LoginRequest request, @NonNull AuthCallback<LoginResponse> callback) {
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Login failed", e);
                postToMainThread(() -> callback.onError("网络错误"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = "";
                if (response.body() != null) {
                    responseBody = response.body().string();
                }

                try {
                    if (!response.isSuccessful()) {
                        // 复用 responseBody
                        LoginResponse errorRes = gson.fromJson(responseBody, LoginResponse.class);
                        String msg = (errorRes != null && errorRes.getMessage() != null)
                                ? errorRes.getMessage()
                                : "登录失败 (" + response.code() + ")";
                        postToMainThread(() -> callback.onError(msg));
                        return;
                    }

                    LoginResponse res = gson.fromJson(responseBody, LoginResponse.class);
                    // 尝试从原始响应 JSON 中提取 token 并保存（增加容错）
                    saveTokenIfPresent(responseBody);
                    if (res != null && res.getCode() == 200 && res.getData() != null) {
                        postToMainThread(() -> callback.onSuccess(res));
                    } else {
                        String msg = (res != null && res.getMessage() != null)
                                ? res.getMessage()
                                : "未知错误";
                        postToMainThread(() -> callback.onError(msg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse login response failed", e);
                    postToMainThread(() -> callback.onError("数据解析失败"));
                }
            }
        });
    }

    /**
     * 执行用户注册操作
     * <p>
     * 将 {@link RegisterRequest} 转换为 JSON 并发送 POST 请求到 /api/auth/register。
     * 成功时返回注册结果；失败时解析错误信息。
     * </p>
     *
     * @param request  注册请求体，包含用户名、手机号和密码
     * @param callback 异步回调接口，用于通知调用者操作结果
     */
    public void register(@NonNull RegisterRequest request, @NonNull AuthCallback<RegisterResponse> callback) {
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/register")
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Register failed", e);
                postToMainThread(() -> callback.onError("网络错误"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        try {
                            String errorBody = response.body() != null ? response.body().string() : "{}";
                            // 尝试解析 message 字段
                            RegisterResponse errorRes = gson.fromJson(errorBody, RegisterResponse.class);
                            String msg = (errorRes != null && errorRes.getMessage() != null)
                                    ? errorRes.getMessage()
                                    : "请求失败 (" + response.code() + ")";
                            postToMainThread(() -> callback.onError(msg));
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error response failed", e);
                            postToMainThread(() ->
                                    callback.onError("服务器错误: " + response.code())
                            );
                        }
                        return;
                    }

                    String responseBody = response.body().string();
                    RegisterResponse res = gson.fromJson(responseBody, RegisterResponse.class);

                    if (res != null && res.getCode() == 200) {
                        postToMainThread(() -> callback.onSuccess(res));
                    } else {
                        String msg = (res != null && res.getMessage() != null) ? res.getMessage() : "注册失败";
                        postToMainThread(() -> callback.onError(msg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse register response failed", e);
                    postToMainThread(() -> callback.onError("数据解析失败"));
                }
            }
        });
    }

    /**
     * 将 Runnable 任务切换到主线程（UI 线程）执行
     * <p>
     * 用于确保回调方法（如 onSuccess/onError）在主线程中被调用，
     * 从而可以直接更新 UI（如 Toast、TextView 等）。
     * </p>
     *
     * @param runnable 要在主线程执行的任务
     */
    private void postToMainThread(Runnable runnable) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
    }

    // 保存 token 到 SharedPreferences
    private void saveTokenIfPresent(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) return;
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            String token = extractTokenFromJson(json);
            if (token != null && !token.isEmpty()) {
                SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_TOKEN, token).apply(); // apply 异步提交
                Log.d(TAG, "Saved token to SharedPreferences");
            } else {
                Log.d(TAG, "No token found in login response");
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to extract/save token", e);
        }
    }

    // 支持多种常见字段名： token, accessToken, data.token, data.accessToken
    private String extractTokenFromJson(JsonObject json) {
        if (json == null) return null;
        String[] keys = {"token", "accessToken", "access_token", "jwt"};
        for (String k : keys) {
            if (json.has(k) && json.get(k) != null && !json.get(k).isJsonNull()) {
                JsonElement el = json.get(k);
                if (el.isJsonPrimitive()) return el.getAsString();
            }
        }
        if (json.has("data") && json.get("data").isJsonObject()) {
            JsonObject data = json.getAsJsonObject("data");
            for (String k : keys) {
                if (data.has(k) && data.get(k) != null && !data.get(k).isJsonNull()) {
                    JsonElement el = data.get(k);
                    if (el.isJsonPrimitive()) return el.getAsString();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前已保存的 Token（用于调试验证）
     *
     * @return 若存在则返回 Token 字符串，否则返回 null
     */
    public String getSavedToken() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * 清除已保存的 Token（在用户退出登录时调用）
     */
    public void clearSavedToken() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    //认证操作的通用回调接口
    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}