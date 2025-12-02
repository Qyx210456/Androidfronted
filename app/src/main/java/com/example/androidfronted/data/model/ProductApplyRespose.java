package com.example.androidfronted.data.model;

/**
 * 贷款申请响应体
 * 对应接口返回：
 * {
 *     "code": 200,
 *     "data": "Application submitted successfully, please wait for review",
 *     "message": "操作成功"
 * }
 */
public class ProductApplyRespose {
    private int code;
    private String data;
    private String message;
    public int getCode() {
        return code;
    }
    public String getData(){return data;}
    public String getMessage() {
        return message;
    }

}
