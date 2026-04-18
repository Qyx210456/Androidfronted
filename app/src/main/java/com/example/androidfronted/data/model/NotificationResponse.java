package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private List<NotificationData> data;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<NotificationData> getData() {
        return data;
    }

    public void setData(List<NotificationData> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class NotificationData {
        @SerializedName("id")
        private int id;

        @SerializedName("userId")
        private int userId;

        @SerializedName("businessId")
        private int businessId;

        @SerializedName("businessType")
        private String businessType;

        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("readFlag")
        private boolean readFlag;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("readAt")
        private String readAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getBusinessId() {
            return businessId;
        }

        public void setBusinessId(int businessId) {
            this.businessId = businessId;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isReadFlag() {
            return readFlag;
        }

        public void setReadFlag(boolean readFlag) {
            this.readFlag = readFlag;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getReadAt() {
            return readAt;
        }

        public void setReadAt(String readAt) {
            this.readAt = readAt;
        }
    }
}
