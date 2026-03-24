package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "auth_tokens")
public class AuthTokenEntity {
    @PrimaryKey
    private int id = 1;
    private String token;
    private String refreshToken;

    public AuthTokenEntity(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
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
}
