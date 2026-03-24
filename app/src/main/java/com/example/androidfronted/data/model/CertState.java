package com.example.androidfronted.data.model;

public enum CertState {
    NOT_CERTIFIED,    // 未认证（显示"立即认证"按钮）
    UPLOADING,        // 认证过程中（显示表单/上传进度）
    CERTIFIED         // 已认证（显示信息 + "修改"按钮）
}
