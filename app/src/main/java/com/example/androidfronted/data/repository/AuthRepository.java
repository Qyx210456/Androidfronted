package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private AuthRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void login(@NonNull LoginRequest request, @NonNull AuthCallback<LoginResponse> callback) {
        remoteDataSource.login(request, new RemoteDataSource.NetworkCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (response != null && response.getData() != null) {
                    String token = response.getData().getToken();
                    if (token != null) {
                        localDataSource.saveToken(token, null);
                    }
                }
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void register(@NonNull RegisterRequest request, @NonNull AuthCallback<RegisterResponse> callback) {
        remoteDataSource.register(request, new RemoteDataSource.NetworkCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void refreshToken(String refreshToken, @NonNull AuthCallback<LoginResponse> callback) {
        remoteDataSource.refreshToken(refreshToken, new RemoteDataSource.NetworkCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (response != null && response.getData() != null) {
                    String token = response.getData().getToken();
                    if (token != null) {
                        localDataSource.saveToken(token, refreshToken);
                    }
                }
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void getCertInfo(@NonNull AuthCallback<CertInfoResponse> callback) {
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity == null || tokenEntity.getToken() == null || tokenEntity.getToken().isEmpty()) {
                    callback.onError("未登录，请先登录");
                    return;
                }
                remoteDataSource.getCertInfo(tokenEntity.getToken(), new RemoteDataSource.NetworkCallback<CertInfoResponse>() {
                    @Override
                    public void onSuccess(CertInfoResponse response) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void submitBasicCert(String idCard, @NonNull AuthCallback<AuthSubmitResponse> callback) {
        Log.d(TAG, "submitBasicCert called, idCard: " + idCard);
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity == null || tokenEntity.getToken() == null || tokenEntity.getToken().isEmpty()) {
                    Log.e(TAG, "submitBasicCert, token is null or empty");
                    callback.onError("未登录，请先登录");
                    return;
                }
                Log.d(TAG, "submitBasicCert, token retrieved successfully");
                remoteDataSource.submitBasicCert(tokenEntity.getToken(), idCard,
                        new RemoteDataSource.NetworkCallback<AuthSubmitResponse>() {
                            @Override
                            public void onSuccess(AuthSubmitResponse response) {
                                Log.d(TAG, "submitBasicCert, onSuccess");
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "submitBasicCert, onError: " + errorMessage);
                                callback.onError(errorMessage);
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "submitBasicCert, getToken error: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void submitOtherCert(String bankCardId,
                          okhttp3.RequestBody propertyFile, okhttp3.RequestBody carFile,
                          okhttp3.RequestBody employmentFile, okhttp3.RequestBody salaryFile,
                          okhttp3.RequestBody socialSecurityFile, okhttp3.RequestBody creditReportFile,
                          @NonNull AuthCallback<AuthSubmitResponse> callback) {
        Log.d(TAG, "submitOtherCert called, bankCardId: " + bankCardId);
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity == null || tokenEntity.getToken() == null || tokenEntity.getToken().isEmpty()) {
                    Log.e(TAG, "submitOtherCert, token is null or empty");
                    callback.onError("未登录，请先登录");
                    return;
                }
                Log.d(TAG, "submitOtherCert, token retrieved successfully");
                remoteDataSource.submitOtherCert(tokenEntity.getToken(), bankCardId, propertyFile, carFile,
                        employmentFile, salaryFile, socialSecurityFile, creditReportFile,
                        new RemoteDataSource.NetworkCallback<AuthSubmitResponse>() {
                            @Override
                            public void onSuccess(AuthSubmitResponse response) {
                                Log.d(TAG, "submitOtherCert, onSuccess");
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "submitOtherCert, onError: " + errorMessage);
                                callback.onError(errorMessage);
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "submitOtherCert, getToken error: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void logout() {
        localDataSource.clearToken();
    }

    public void getToken(@NonNull LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity> callback) {
        localDataSource.getToken(callback);
    }

    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}
