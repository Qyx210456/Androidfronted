package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 还款计划接口响应模型
 * 对应接口: GET /api/orders/{orderId}/repayment-plan
 */
public class RepaymentPlanResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private List<RepaymentPlanItem> data;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<RepaymentPlanItem> getData() {
        return data;
    }

    public void setData(List<RepaymentPlanItem> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 单期还款计划数据
     */
    public static class RepaymentPlanItem {
        @SerializedName("id")
        private long id;

        @SerializedName("orderId")
        private long orderId;

        @SerializedName("term")
        private int term;

        @SerializedName("principal")
        private double principal;

        @SerializedName("interest")
        private double interest;

        @SerializedName("totalAmount")
        private double totalAmount;

        @SerializedName("status")
        private String status;

        @SerializedName("remainingPrincipal")
        private double remainingPrincipal;

        @SerializedName("remainingInterest")
        private double remainingInterest;

        @SerializedName("dueDate")
        private String dueDate;

        @SerializedName("actualPayDate")
        private String actualPayDate;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getOrderId() {
            return orderId;
        }

        public void setOrderId(long orderId) {
            this.orderId = orderId;
        }

        public int getTerm() {
            return term;
        }

        public void setTerm(int term) {
            this.term = term;
        }

        public double getPrincipal() {
            return principal;
        }

        public void setPrincipal(double principal) {
            this.principal = principal;
        }

        public double getInterest() {
            return interest;
        }

        public void setInterest(double interest) {
            this.interest = interest;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getRemainingPrincipal() {
            return remainingPrincipal;
        }

        public void setRemainingPrincipal(double remainingPrincipal) {
            this.remainingPrincipal = remainingPrincipal;
        }

        public double getRemainingInterest() {
            return remainingInterest;
        }

        public void setRemainingInterest(double remainingInterest) {
            this.remainingInterest = remainingInterest;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getActualPayDate() {
            return actualPayDate;
        }

        public void setActualPayDate(String actualPayDate) {
            this.actualPayDate = actualPayDate;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
