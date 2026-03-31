package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.local.dao.UserDao;
import com.example.androidfronted.data.local.entity.UserEntity;
import com.example.androidfronted.data.model.UserInfoResponse;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;
import okhttp3.RequestBody;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static UserRepository instance;
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private UserRepository(Context context) {
        this.localDataSource = new LocalDataSource(context.getApplicationContext());
        this.remoteDataSource = new RemoteDataSource(context.getApplicationContext());
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }

    /**
     * 获取用户信息
     * 先从本地数据库获取，然后从网络获取最新数据
     */
    public void getUserInfo(@NonNull AuthCallback<UserInfoResponse> callback) {
        Log.d(TAG, "getUserInfo called");
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity != null && tokenEntity.getToken() != null) {
                    // 从网络获取最新用户信息
                    remoteDataSource.getUserInfo(tokenEntity.getToken(), new RemoteDataSource.NetworkCallback<UserInfoResponse>() {
                        @Override
                        public void onSuccess(UserInfoResponse response) {
                            if (response != null && response.getCode() == 200 && response.getData() != null) {
                                // 更新本地数据库
                                UserInfoResponse.UserData userData = response.getData();
                                UserEntity userEntity = new UserEntity(
                                        userData.getUserId(),
                                        userData.getUsername(),
                                        userData.getAvatar()
                                );
                                localDataSource.saveUser(userEntity);
                            }
                            callback.onSuccess(response);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // 网络请求失败，从本地获取
                            localDataSource.getUser(new LocalDataSource.DataSourceCallback<UserEntity>() {
                                @Override
                                public void onSuccess(UserEntity userEntity) {
                                    if (userEntity != null) {
                                        UserInfoResponse response = new UserInfoResponse();
                                        response.setCode(200);
                                        UserInfoResponse.UserData userData = new UserInfoResponse.UserData();
                                        userData.setUserId(userEntity.getUserId());
                                        userData.setUsername(userEntity.getUserName());
                                        userData.setAvatar(userEntity.getAvatar());
                                        userData.setPhone("");
                                        response.setData(userData);
                                        response.setMessage("Success");
                                        callback.onSuccess(response);
                                    } else {
                                        callback.onError(errorMessage);
                                    }
                                }

                                @Override
                                public void onError(String localError) {
                                    callback.onError(errorMessage);
                                }
                            });
                        }
                    });
                } else {
                    callback.onError("No token available");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(String username, RequestBody avatar, @NonNull AuthCallback<UserInfoResponse> callback) {
        Log.d(TAG, "updateUserInfo called, username: " + username);
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity != null && tokenEntity.getToken() != null) {
                    remoteDataSource.updateUserInfo(tokenEntity.getToken(), username, avatar, new RemoteDataSource.NetworkCallback<UserInfoResponse>() {
                        @Override
                        public void onSuccess(UserInfoResponse response) {
                            if (response != null && response.getCode() == 200 && response.getData() != null) {
                                // 更新本地数据库
                                UserInfoResponse.UserData userData = response.getData();
                                UserEntity userEntity = new UserEntity(
                                        userData.getUserId(),
                                        userData.getUsername(),
                                        userData.getAvatar()
                                );
                                localDataSource.saveUser(userEntity);
                            }
                            callback.onSuccess(response);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError(errorMessage);
                        }
                    });
                } else {
                    callback.onError("No token available");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * 认证回调接口
     * @param <T> 回调数据类型
     */
    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}