package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * 订单统计响应（GET /api/orders/statistics）
 * 统计当前用户所有订单的未还期数金额合计
 */
public class OrderStatisticsResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private StatisticsData data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsData getData() {
        return data;
    }

    public static class StatisticsData {
        /** 待还总额：Σ 未还期数 principal + interest */
        @SerializedName("totalOutstandingAmount")
        private Double totalOutstandingAmount;

        /** 待还本金：Σ 未还期数 principal */
        @SerializedName("totalOutstandingPrincipal")
        private Double totalOutstandingPrincipal;

        /** 待还利息：Σ 未还期数 interest */
        @SerializedName("totalOutstandingInterest")
        private Double totalOutstandingInterest;

        public Double getTotalOutstandingAmount() {
            return totalOutstandingAmount;
        }

        public Double getTotalOutstandingPrincipal() {
            return totalOutstandingPrincipal;
        }

        public Double getTotalOutstandingInterest() {
            return totalOutstandingInterest;
        }
    }
}
