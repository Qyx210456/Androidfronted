package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notifications")
public class NotificationEntity implements Serializable {
    @PrimaryKey
    private int id;
    private int userId;
    private int businessId;
    private String businessType;
    private String title;
    private String content;
    private boolean readFlag;
    private String createdAt;
    private String readAt;

    public NotificationEntity(int id, int userId, int businessId, String businessType,
                              String title, String content, boolean readFlag,
                              String createdAt, String readAt) {
        this.id = id;
        this.userId = userId;
        this.businessId = businessId;
        this.businessType = businessType;
        this.title = title;
        this.content = content;
        this.readFlag = readFlag;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

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
