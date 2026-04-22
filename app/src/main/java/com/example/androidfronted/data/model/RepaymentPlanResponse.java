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
        @SerializedName("term")
        private int term;

        @SerializedName("principal")
        private double principal;

        @SerializedName("interest")
        private double interest;

        @SerializedName("total")
        private double total;

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

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}
