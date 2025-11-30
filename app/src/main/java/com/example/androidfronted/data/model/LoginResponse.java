package com.example.androidfronted.data.model;

/**
 * 登录响应体
 * 对应接口返回：
 * {
 *   "code": 200,
 *   "data": { "token": "xxx" },
 *   "message": "登录成功"
 * }
 */
public class LoginResponse {
    private int code;
    private LoginData data;
    private String message;

    public int getCode() {
        return code;
    }

    public LoginData getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public static class LoginData {
        private String token;

        public String getToken() {
            return token;
        }
    }
}