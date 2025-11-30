package com.example.androidfronted.data.model;

import java.util.List;

/**
 * 贷款产品列表响应
 * {
 *   "code": 200,
 *   "data": [ { "id": 1, "name": "...", ... } ],
 *   "message": "success"
 * }
 */
public class LoanProductResponse {
    private int code;
    private List<LoanProduct> data;
    private String message;

    public int getCode() { return code; }
    public List<LoanProduct> getData() { return data; }
    public String getMessage() { return message; }
}