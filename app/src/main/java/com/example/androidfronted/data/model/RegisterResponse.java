package com.example.androidfronted.data.model;

/**
 * 注册响应体
 * 对应接口返回：
 * {
 *   "code": 200,
 *   "data": {
 *     "id": 11,
 *     "name": "Tom",
 *     "createTime": "2025-11-18T14:47:41.9154566"
 *   },
 *   "message": "注册成功"
 * }
 */
public class RegisterResponse {
    private int code;
    private RegisterData data;
    private String message;

    public int getCode() {
        return code;
    }

    public RegisterData getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public static class RegisterData {
        private long id;
        private String name;
        private String createTime;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCreateTime() {
            return createTime;
        }
    }
}