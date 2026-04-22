package com.example.androidfronted.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;

/**
 * Token 管理工具类
 * <p>
 * 负责安全地存储、读取和清除用户登录后返回的 JWT token 和 refreshToken。
 * 使用 SharedPreferences 实现本地持久化，避免每次启动都重新登录。
 * </p>
 */
public class TokenManager {
    private static final String TAG = "TokenManager";

    /**
     * SharedPreferences 文件名
     */
    private static final String PREF_NAME = "auth_prefs";

    /**
     * 存储 token 的键名
     */
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

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

    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveRefreshToken(String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveTokens(String token, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }

    public void clearToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public boolean hasRefreshToken() {
        String refreshToken = getRefreshToken();
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    public boolean isTokenExpired() {
        String token = getToken();
        Log.d(TAG, "isTokenExpired() called, token: " + (token != null ? "exists" : "null"));
        
        if (token == null || token.trim().isEmpty() || "null".equalsIgnoreCase(token)) {
            Log.d(TAG, "Token is null or empty, considering as expired");
            return true;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.d(TAG, "Token format invalid (parts < 2), considering as expired");
                return true;
            }

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING));
            JSONObject json = new JSONObject(payload);

            long exp = json.optLong("exp", 0);
            if (exp <= 0) {
                Log.d(TAG, "Token exp field invalid (exp <= 0), considering as expired");
                return true;
            }

            long currentTimeSec = System.currentTimeMillis() / 1000;
            boolean expired = currentTimeSec > exp;
            Log.d(TAG, "Token exp: " + exp + ", current: " + currentTimeSec + ", expired: " + expired);
            return expired;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing token, considering as expired: " + e.getMessage());
            return true;
        }
    }

    public boolean isRefreshTokenExpired() {
        String refreshToken = getRefreshToken();
        Log.d(TAG, "isRefreshTokenExpired() called, refreshToken: " + (refreshToken != null ? "exists" : "null"));
        
        if (refreshToken == null || refreshToken.trim().isEmpty() || "null".equalsIgnoreCase(refreshToken)) {
            Log.d(TAG, "RefreshToken is null or empty, considering as expired");
            return true;
        }

        try {
            String[] parts = refreshToken.split("\\.");
            if (parts.length < 2) {
                Log.d(TAG, "RefreshToken format invalid (parts < 2), considering as expired");
                return true;
            }

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING));
            JSONObject json = new JSONObject(payload);

            long exp = json.optLong("exp", 0);
            if (exp <= 0) {
                Log.d(TAG, "RefreshToken exp field invalid (exp <= 0), considering as expired");
                return true;
            }

            long currentTimeSec = System.currentTimeMillis() / 1000;
            boolean expired = currentTimeSec > exp;
            Log.d(TAG, "RefreshToken exp: " + exp + ", current: " + currentTimeSec + ", expired: " + expired);
            return expired;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing refreshToken, considering as expired: " + e.getMessage());
            return true;
        }
    }

    public boolean isTokenValid() {
        boolean hasToken = hasToken();
        boolean expired = isTokenExpired();
        boolean valid = hasToken && !expired;
        Log.d(TAG, "isTokenValid: hasToken=" + hasToken + ", expired=" + expired + ", valid=" + valid);
        return valid;
    }

    public boolean isRefreshTokenValid() {
        boolean hasRefreshToken = hasRefreshToken();
        boolean expired = isRefreshTokenExpired();
        boolean valid = hasRefreshToken && !expired;
        Log.d(TAG, "isRefreshTokenValid: hasRefreshToken=" + hasRefreshToken + ", expired=" + expired + ", valid=" + valid);
        return valid;
    }
}
