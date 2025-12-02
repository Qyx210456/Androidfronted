package com.example.androidfronted.data.model;

/**
 * 贷款申请请求体
 * 对应接口：POST /api/loan-applications
 * 字段：productId,optionId,term
 */
public class ProductApplyRequest {
    private final int productId;
    private final int optionId;
    private final int term;
    public ProductApplyRequest(int productId,int optionId,int term){
        this.productId=productId;
        this.optionId=optionId;
        this.term=term;
    }
}

