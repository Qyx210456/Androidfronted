package com.example.androidfronted.data.model;

/**
 * 注册请求体
 * 对应接口：POST /api/auth/register
 * 字段：name, phone, password
 */
public class RegisterRequest {
    private final String name;
    private final String phone;
    private final String password;

    public RegisterRequest(String name, String phone, String password) {
        this.name = name;
        this.phone = phone;
        this.password = password;
    }
}
