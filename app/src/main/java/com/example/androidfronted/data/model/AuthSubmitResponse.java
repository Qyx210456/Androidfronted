package com.example.androidfronted.data.model;

/**
 * 上传认证材料响应体
 * 对应接口: POST /api/auth/submit-all
 * 示例 JSON:
 * {
 *   "code": 200,
 *   "data": null,
 *   "message": "全部认证材料提交成功"
 * }
 */
public class AuthSubmitResponse {
    private int code;
    private Object data; // 通常为 null
    private String message;

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}