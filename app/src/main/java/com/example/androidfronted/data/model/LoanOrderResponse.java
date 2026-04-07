package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoanOrderResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private List<LoanOrder> data;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<LoanOrder> getData() {
        return data;
    }

    public void setData(List<LoanOrder> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class LoanOrder {
        @SerializedName("id")
        private int id;

        @SerializedName("loanAmount")
        private double loanAmount;

        @SerializedName("status")
        private String status;

        @SerializedName("startTime")
        private String startTime;

        @SerializedName("term")
        private int term;

        @SerializedName("currentTerm")
        private int currentTerm;

        @SerializedName("overdueDays")
        private int overdueDays;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getLoanAmount() {
            return loanAmount;
        }

        public void setLoanAmount(double loanAmount) {
            this.loanAmount = loanAmount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
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

        public int getOverdueDays() {
            return overdueDays;
        }

        public void setOverdueDays(int overdueDays) {
            this.overdueDays = overdueDays;
        }
    }
}
