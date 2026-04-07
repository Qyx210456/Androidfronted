package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

public class ApplicationDetailResponse {
    @SerializedName("code")
    private int code;
    
    @SerializedName("data")
    private ApplicationDetail data;
    
    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ApplicationDetail getData() {
        return data;
    }

    public void setData(ApplicationDetail data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class ApplicationDetail {
        @SerializedName("id")
        private int id;
        
        @SerializedName("userId")
        private int userId;
        
        @SerializedName("productId")
        private int productId;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("loanAmount")
        private double loanAmount;
        
        @SerializedName("interestRate")
        private double interestRate;
        
        @SerializedName("loanPeriod")
        private int loanPeriod;
        
        @SerializedName("term")
        private int term;
        
        @SerializedName("repaidType")
        private String repaidType;
        
        @SerializedName("rejectReason")
        private String rejectReason;
        
        @SerializedName("applyTime")
        private String applyTime;
        
        @SerializedName("reviewTime")
        private String reviewTime;

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

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getLoanAmount() {
            return loanAmount;
        }

        public void setLoanAmount(double loanAmount) {
            this.loanAmount = loanAmount;
        }

        public double getInterestRate() {
            return interestRate;
        }

        public void setInterestRate(double interestRate) {
            this.interestRate = interestRate;
        }

        public int getLoanPeriod() {
            return loanPeriod;
        }

        public void setLoanPeriod(int loanPeriod) {
            this.loanPeriod = loanPeriod;
        }

        public int getTerm() {
            return term;
        }

        public void setTerm(int term) {
            this.term = term;
        }

        public String getRepaidType() {
            return repaidType;
        }

        public void setRepaidType(String repaidType) {
            this.repaidType = repaidType;
        }

        public String getRejectReason() {
            return rejectReason;
        }

        public void setRejectReason(String rejectReason) {
            this.rejectReason = rejectReason;
        }

        public String getApplyTime() {
            return applyTime;
        }

        public void setApplyTime(String applyTime) {
            this.applyTime = applyTime;
        }

        public String getReviewTime() {
            return reviewTime;
        }

        public void setReviewTime(String reviewTime) {
            this.reviewTime = reviewTime;
        }
    }
}