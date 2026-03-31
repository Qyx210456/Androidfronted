package com.example.androidfronted.data.source;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.model.ProductApplyRequest;
import com.example.androidfronted.data.model.ProductApplyResponse;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.data.model.UserInfoResponse;
import com.example.androidfronted.network.NetworkClient;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteDataSource {
    private static final String TAG = "RemoteDataSource";
    private static final String BASE_URL = "http://10.0.2.2:8080/api";

    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public RemoteDataSource(Context context) {
        this.client = NetworkClient.getOkHttpClient(context.getApplicationContext());
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 执行HTTP请求的通用方法
     * @param request HTTP请求对象
     * @param requestName 请求名称，用于日志记录
     * @param responseClass 响应数据的类型
     * @param callback 网络回调接口
     * @param <T> 响应数据类型
     */
    private <T> void executeRequest(Request request, String requestName, Class<T> responseClass, final NetworkCallback<T> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, requestName + " failed", e);
                mainHandler.post(() -> callback.onError("网络错误"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "{}";
                try {
                    if (!response.isSuccessful()) {
                        mainHandler.post(() -> callback.onError("请求失败: " + response.code()));
                        return;
                    }

                    T res = gson.fromJson(responseBody, responseClass);
                    mainHandler.post(() -> callback.onSuccess(res));
                } catch (Exception e) {
                    Log.e(TAG, "Parse " + requestName + " response failed", e);
                    mainHandler.post(() -> callback.onError("数据解析失败"));
                }
            }
        });
    }

    /**
     * 执行HTTP请求并进行错误处理的通用方法
     * 与executeRequest不同，此方法会从响应体中提取错误消息
     * @param request HTTP请求对象
     * @param requestName 请求名称，用于日志记录
     * @param responseClass 响应数据的类型
     * @param callback 网络回调接口
     * @param <T> 响应数据类型
     */
    private <T> void executeRequestWithErrorHandling(Request request, String requestName, Class<T> responseClass, final NetworkCallback<T> callback) {
        Log.d(TAG, "executeRequestWithErrorHandling called, requestName: " + requestName);
        Log.d(TAG, "executeRequestWithErrorHandling, request URL: " + request.url());
        Log.d(TAG, "executeRequestWithErrorHandling, request method: " + request.method());
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, requestName + " failed", e);
                Log.e(TAG, requestName + " error message: " + e.getMessage());
                Log.e(TAG, requestName + " error type: " + e.getClass().getSimpleName());
                mainHandler.post(() -> callback.onError("网络错误"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "{}";
                Log.d(TAG, requestName + " onResponse, response code: " + response.code());
                Log.d(TAG, requestName + " onResponse, response message: " + response.message());
                Log.d(TAG, requestName + " onResponse, response body: " + responseBody);
                
                try {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, requestName + " response not successful, code: " + response.code());
                        T errorRes = gson.fromJson(responseBody, responseClass);
                        String msg = extractErrorMessage(errorRes, response.code());
                        Log.e(TAG, requestName + " error message from response: " + msg);
                        mainHandler.post(() -> callback.onError(msg));
                        return;
                    }

                    Log.d(TAG, requestName + " response successful, parsing response");
                    T res = gson.fromJson(responseBody, responseClass);
                    Log.d(TAG, requestName + " response parsed successfully");
                    mainHandler.post(() -> callback.onSuccess(res));
                } catch (Exception e) {
                    Log.e(TAG, "Parse " + requestName + " response failed", e);
                    Log.e(TAG, "Parse " + requestName + " response body: " + responseBody);
                    mainHandler.post(() -> callback.onError("数据解析失败"));
                }
            }
        });
    }

    /**
     * 执行HTTP请求并提取特定数据的通用方法
     * 此方法允许从响应对象中提取特定字段，并进行数据验证
     * @param request HTTP请求对象
     * @param requestName 请求名称，用于日志记录
     * @param responseClass 响应数据的类型
     * @param extractor 数据提取器，用于从响应中提取和验证数据
     * @param callback 网络回调接口
     * @param <T> 原始响应数据类型
     * @param <R> 提取后的数据类型
     */
    private <T, R> void executeRequestWithDataExtraction(Request request, String requestName, Class<T> responseClass, 
                                                      DataExtractor<T, R> extractor, final NetworkCallback<R> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, requestName + " failed", e);
                mainHandler.post(() -> callback.onError("网络错误"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "{}";
                try {
                    if (!response.isSuccessful()) {
                        T errorRes = gson.fromJson(responseBody, responseClass);
                        String msg = extractErrorMessage(errorRes, response.code());
                        mainHandler.post(() -> callback.onError(msg));
                        return;
                    }

                    T res = gson.fromJson(responseBody, responseClass);
                    
                    if (!extractor.validate(res)) {
                        String msg = extractErrorMessage(res, response.code());
                        mainHandler.post(() -> callback.onError(msg));
                        return;
                    }
                    
                    R data = extractor.extract(res);
                    mainHandler.post(() -> callback.onSuccess(data));
                } catch (Exception e) {
                    Log.e(TAG, "Parse " + requestName + " response failed. Response: " + responseBody, e);
                    mainHandler.post(() -> callback.onError("数据解析失败"));
                }
            }
        });
    }

    /**
     * 数据提取器接口
     * 用于从响应对象中提取特定数据并进行验证
     * @param <T> 原始响应数据类型
     * @param <R> 提取后的数据类型
     */
    private interface DataExtractor<T, R> {
        /**
         * 从响应对象中提取数据
         * @param response 原始响应对象
         * @return 提取后的数据
         */
        R extract(T response);
        
        /**
         * 验证响应数据是否有效
         * @param response 原始响应对象
         * @return true表示验证通过，false表示验证失败
         */
        default boolean validate(T response) {
            return true;
        }
    }

    private <T> String extractErrorMessage(T response, int statusCode) {
        if (response instanceof LoginResponse) {
            LoginResponse res = (LoginResponse) response;
            return (res != null && res.getMessage() != null) ? res.getMessage() : "请求失败 (" + statusCode + ")";
        } else if (response instanceof RegisterResponse) {
            RegisterResponse res = (RegisterResponse) response;
            return (res != null && res.getMessage() != null) ? res.getMessage() : "请求失败 (" + statusCode + ")";
        } else if (response instanceof ProductApplyResponse) {
            ProductApplyResponse res = (ProductApplyResponse) response;
            return (res != null && res.getMessage() != null) ? res.getMessage() : "提交失败";
        } else if (response instanceof UserInfoResponse) {
            UserInfoResponse res = (UserInfoResponse) response;
            return (res != null && res.getMessage() != null) ? res.getMessage() : "请求失败 (" + statusCode + ")";
        }
        return "请求失败 (" + statusCode + ")";
    }

    public void login(LoginRequest request, final NetworkCallback<LoginResponse> callback) {
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/auth/login")
                .post(body)
                .build();

        executeRequestWithErrorHandling(httpRequest, "Login", LoginResponse.class, callback);
    }

    public void register(RegisterRequest request, final NetworkCallback<RegisterResponse> callback) {
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/auth/register")
                .post(body)
                .build();

        executeRequestWithErrorHandling(httpRequest, "Register", RegisterResponse.class, callback);
    }

    public void getCertInfo(String token, final NetworkCallback<CertInfoResponse> callback) {
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/auth/cert-info")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeRequest(httpRequest, "Get cert info", CertInfoResponse.class, callback);
    }

    public void submitBasicCert(String token, String idCard, final NetworkCallback<AuthSubmitResponse> callback) {
        Log.d(TAG, "submitBasicCert called, idCard: " + idCard);
        Log.d(TAG, "submitBasicCert, token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        Log.d(TAG, "submitBasicCert, BASE_URL: " + BASE_URL);
        
        okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("idCard", idCard);

        okhttp3.RequestBody requestBody = builder.build();
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/auth/submit-basic")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        Log.d(TAG, "submitBasicCert, request URL: " + httpRequest.url());
        executeRequestWithErrorHandling(httpRequest, "Submit basic cert", AuthSubmitResponse.class, callback);
    }

    public void submitOtherCert(String token, String bankCardId,
                            okhttp3.RequestBody propertyFile, okhttp3.RequestBody carFile,
                            okhttp3.RequestBody employmentFile, okhttp3.RequestBody salaryFile,
                            okhttp3.RequestBody socialSecurityFile, okhttp3.RequestBody creditReportFile,
                            final NetworkCallback<AuthSubmitResponse> callback) {
        Log.d(TAG, "submitOtherCert called, bankCardId: " + bankCardId);
        Log.d(TAG, "submitOtherCert, token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        Log.d(TAG, "submitOtherCert, BASE_URL: " + BASE_URL);
        Log.d(TAG, "submitOtherCert, propertyFile: " + (propertyFile != null ? "present" : "null"));
        Log.d(TAG, "submitOtherCert, carFile: " + (carFile != null ? "present" : "null"));
        Log.d(TAG, "submitOtherCert, employmentFile: " + (employmentFile != null ? "present" : "null"));
        Log.d(TAG, "submitOtherCert, salaryFile: " + (salaryFile != null ? "present" : "null"));
        Log.d(TAG, "submitOtherCert, socialSecurityFile: " + (socialSecurityFile != null ? "present" : "null"));
        Log.d(TAG, "submitOtherCert, creditReportFile: " + (creditReportFile != null ? "present" : "null"));
        
        okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("bankCardId", bankCardId);

        if (propertyFile != null) {
            String extension = getFileExtension(propertyFile);
            builder.addFormDataPart("propertyFile", "property_" + System.currentTimeMillis() + extension, propertyFile);
        } else {
            builder.addFormDataPart("propertyFile", "");
        }
        if (carFile != null) {
            String extension = getFileExtension(carFile);
            builder.addFormDataPart("carFile", "car_" + System.currentTimeMillis() + extension, carFile);
        } else {
            builder.addFormDataPart("carFile", "");
        }
        if (employmentFile != null) {
            String extension = getFileExtension(employmentFile);
            builder.addFormDataPart("employmentFile", "employment_" + System.currentTimeMillis() + extension, employmentFile);
        } else {
            builder.addFormDataPart("employmentFile", "");
        }
        if (salaryFile != null) {
            String extension = getFileExtension(salaryFile);
            builder.addFormDataPart("salaryFile", "salary_" + System.currentTimeMillis() + extension, salaryFile);
        } else {
            builder.addFormDataPart("salaryFile", "");
        }
        if (socialSecurityFile != null) {
            String extension = getFileExtension(socialSecurityFile);
            builder.addFormDataPart("socialSecurityFile", "social_security_" + System.currentTimeMillis() + extension, socialSecurityFile);
        } else {
            builder.addFormDataPart("socialSecurityFile", "");
        }
        if (creditReportFile != null) {
            String extension = getFileExtension(creditReportFile);
            builder.addFormDataPart("creditReportFile", "credit_report_" + System.currentTimeMillis() + extension, creditReportFile);
        } else {
            builder.addFormDataPart("creditReportFile", "");
        }

        okhttp3.RequestBody requestBody = builder.build();
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/auth/submit-other")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        Log.d(TAG, "submitOtherCert, request URL: " + httpRequest.url());
        executeRequestWithErrorHandling(httpRequest, "Submit other cert", AuthSubmitResponse.class, callback);
    }

    public void refreshToken(String refreshToken, final NetworkCallback<LoginResponse> callback) {
        String json = gson.toJson(new RefreshTokenRequest(refreshToken));
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/auth/refresh-token")
                .post(body)
                .build();

        executeRequest(httpRequest, "Refresh token", LoginResponse.class, callback);
    }

    public void getLoanProducts(final NetworkCallback<LoanProductResponse> callback) {
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/loan-products/user")
                .get()
                .build();

        executeRequest(httpRequest, "Get loan products", LoanProductResponse.class, callback);
    }

    public void submitLoanApplication(ProductApplyRequest request, final NetworkCallback<String> callback) {
        String jsonBody = gson.toJson(request);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/loan-applications")
                .post(body)
                .build();

        executeRequestWithDataExtraction(httpRequest, "Submit loan application", ProductApplyResponse.class,
                new DataExtractor<ProductApplyResponse, String>() {
                    @Override
                    public String extract(ProductApplyResponse response) {
                        return response.getData();
                    }

                    @Override
                    public boolean validate(ProductApplyResponse response) {
                        return response != null && response.getCode() == 200;
                    }
                }, callback);
    }

    public void getUserInfo(String token, final NetworkCallback<UserInfoResponse> callback) {
        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/users/me")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeRequest(httpRequest, "Get user info", UserInfoResponse.class, callback);
    }

    public void updateUserInfo(String token, String username, okhttp3.RequestBody avatar, final NetworkCallback<UserInfoResponse> callback) {
        if (avatar != null) {
            okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("file", "avatar_" + System.currentTimeMillis() + ".jpg", avatar);

            okhttp3.RequestBody requestBody = builder.build();
            Request httpRequest = new Request.Builder()
                    .url(BASE_URL + "/users/avatar")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(requestBody)
                    .build();

            executeRequestWithErrorHandling(httpRequest, "Update avatar", UserInfoResponse.class, callback);
        } else if (username != null && !username.isEmpty()) {
            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("text/plain"), username);

            Request httpRequest = new Request.Builder()
                    .url(BASE_URL + "/users/me")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "text/plain")
                    .post(requestBody)
                    .build();

            executeRequestWithErrorHandling(httpRequest, "Update username", UserInfoResponse.class, callback);
        }
    }

    private static class RefreshTokenRequest {
        private final String refreshToken;

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    private String getFileExtension(okhttp3.RequestBody requestBody) {
        return ".jpg";
    }

    public interface NetworkCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}
