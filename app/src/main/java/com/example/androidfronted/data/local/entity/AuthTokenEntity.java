package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "auth_tokens")
public class AuthTokenEntity {
    @PrimaryKey
    private int id = 1;
    private String token;
    private String refreshToken;
    private Integer userId;

    public AuthTokenEntity(String token, String refreshToken, Integer userId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }
    
    // 保持向后兼容的构造函数
    @Ignore
    public AuthTokenEntity(String token, String refreshToken) {
        this(token, refreshToken, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
