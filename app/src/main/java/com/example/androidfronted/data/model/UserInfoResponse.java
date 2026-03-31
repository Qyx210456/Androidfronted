package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

public class UserInfoResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private UserData data;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class UserData {
        @SerializedName("userId")
        private int userId;

        @SerializedName("userName")
        private String username;

        @SerializedName("avatar")
        private String avatar;

        @SerializedName("phone")
        private String phone;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}