package com.example.androidfronted.data.model;

/**
 * 贷款申请请求体
 * 对应接口：POST /api/loan-applications
 * 字段：productId,optionId,term,loanAmount
 */
public class ProductApplyRequest {
    private final int productId;
    private final int optionId;
    private final int term;
    private final double loanAmount;
    
    public ProductApplyRequest(int productId, int optionId, int term, double loanAmount){
        this.productId = productId;
        this.optionId = optionId;
        this.term = term;
        this.loanAmount = loanAmount;
    }

    public int getProductId() {
        return productId;
    }

    public int getOptionId() {
        return optionId;
    }

    public int getTerm() {
        return term;
    }

    public double getLoanAmount() {
        return loanAmount;
    }
}

