package com.example.androidfronted.data.model;

/**
 * 登录请求体
 * 对应接口：POST /api/auth/login
 * 字段：phone, password
 */
public class LoginRequest {
    private final String phone;
    private final String password;

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}