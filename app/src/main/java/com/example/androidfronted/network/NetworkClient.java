package com.example.androidfronted.network;

import android.content.Context;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

/**
 * 网络客户端管理类
 * 提供全局OkHttpClient实例，添加认证拦截器
 */
public class NetworkClient {
    private static OkHttpClient sClient;

    /**
     * 获取配置好的OkHttpClient实例
     * @param context 上下文，用于认证拦截器
     * @return 单例OkHttpClient
     */
    public static synchronized OkHttpClient getOkHttpClient(Context context) {
        if (sClient == null) {
            sClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context)) // 添加认证拦截器
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        return sClient;
    }
}