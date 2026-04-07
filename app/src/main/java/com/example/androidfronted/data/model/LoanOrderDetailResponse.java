package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

public class LoanOrderDetailResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private LoanOrderDetailData data;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public LoanOrderDetailData getData() {
        return data;
    }

    public void setData(LoanOrderDetailData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class LoanOrderDetailData {
        @SerializedName("productName")
        private String productName;

        @SerializedName("order")
        private OrderDetail order;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public OrderDetail getOrder() {
            return order;
        }

        public void setOrder(OrderDetail order) {
            this.order = order;
        }
    }

    public static class OrderDetail {
        @SerializedName("id")
        private int id;

        @SerializedName("userId")
        private int userId;

        @SerializedName("productId")
        private int productId;

        @SerializedName("status")
        private String status;

        @SerializedName("repaidAmount")
        private double repaidAmount;

        @SerializedName("loanAmount")
        private double loanAmount;

        @SerializedName("interestRate")
        private double interestRate;

        @SerializedName("repaidType")
        private String repaidType;

        @SerializedName("loanPeriod")
        private int loanPeriod;

        @SerializedName("term")
        private int term;

        @SerializedName("currentTerm")
        private int currentTerm;

        @SerializedName("contract")
        private String contract;

        @SerializedName("overdueDays")
        private int overdueDays;

        @SerializedName("startTime")
        private String startTime;

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

        public double getRepaidAmount() {
            return repaidAmount;
        }

        public void setRepaidAmount(double repaidAmount) {
            this.repaidAmount = repaidAmount;
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

        public String getRepaidType() {
            return repaidType;
        }

        public void setRepaidType(String repaidType) {
            this.repaidType = repaidType;
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

        public int getCurrentTerm() {
            return currentTerm;
        }

        public void setCurrentTerm(int currentTerm) {
            this.currentTerm = currentTerm;
        }

        public String getContract() {
            return contract;
        }

        public void setContract(String contract) {
            this.contract = contract;
        }

        public int getOverdueDays() {
            return overdueDays;
        }

        public void setOverdueDays(int overdueDays) {
            this.overdueDays = overdueDays;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    }
}
