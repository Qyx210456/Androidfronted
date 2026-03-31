package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

public class AvatarUpdateResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private String data;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
