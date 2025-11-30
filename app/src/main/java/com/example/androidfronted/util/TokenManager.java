package com.example.androidfronted.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import org.json.JSONObject;

/**
 * Token 管理工具类
 * <p>
 * 负责安全地存储、读取和清除用户登录后返回的 JWT token。
 * 使用 SharedPreferences 实现本地持久化，避免每次启动都重新登录。
 * </p>
 */
public class TokenManager {

    /**
     * SharedPreferences 文件名
     */
    private static final String PREF_NAME = "auth_prefs";

    /**
     * 存储 token 的键名
     */
    private static final String KEY_TOKEN = "auth_token";

    /**
     * 应用上下文中的 SharedPreferences 实例
     */
    private final SharedPreferences prefs;

    /**
     * 构造函数，初始化 SharedPreferences
     *
     * @param context 应用上下文（传入 ApplicationContext）
     */
    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 将 JWT token 保存到本地存储
     *
     * @param token 登录接口返回的有效 token 字符串
     */
    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply(); // 使用apply，异步保存
    }

    /**
     * 从本地存储中获取已保存的 token
     *
     * @return 若存在则返回 token 字符串；否则返回 null
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * 清除本地存储的 token（用于退出登录）
     */
    public void clearToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

    // 调试方法
    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    /**
     * 判断 Token 是否已过期（基于 JWT 的 exp 字段）
     * JWT 的 exp 是秒级时间戳
     *
     * @return true 表示已过期或无效；false 表示未过期
     */
    public boolean isTokenExpired() {
        String token = getToken();
        if (token == null || token.trim().isEmpty() || "null".equalsIgnoreCase(token)) {
            return true;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return true; // 不是合法的 JWT
            }

            // 解码 payload（第二部分）
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING));
            JSONObject json = new JSONObject(payload);

            long exp = json.optLong("exp", 0);
            if (exp <= 0) {
                return true; // 没有 exp 字段或无效
            }

            // 获取当前 UTC 时间（毫秒 -> 秒）
            long currentTimeSec = System.currentTimeMillis() / 1000;
            return currentTimeSec > exp;

        } catch (Exception e) {
            // 解析失败（如不是 JWT、格式错误等），视为已过期/无效
            return true;
        }
    }

    /**
     * 判断 Token 是否有效：
     * - 存在
     * - 非空
     * - 不是 "null" 字符串
     * - 未过期（基于 exp）
     *
     * @return true 表示有效；false 表示无效或已过期
     */
    public boolean isTokenValid() {
        return hasToken() && !isTokenExpired();
    }
}
