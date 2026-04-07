package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ApplicationListResponse {
    private int code;
    private List<ApplicationRecord> data;
    private String message;

    public int getCode() { return code; }
    public List<ApplicationRecord> getData() { return data; }
    public String getMessage() { return message; }

    public static class ApplicationRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("applicationId")
        private int applicationId;

        @SerializedName("productName")
        private String productName;

        @SerializedName("loanAmount")
        private double loanAmount;

        @SerializedName("status")
        private String status;

        @SerializedName("applyTime")
        private String applyTime;

        @SerializedName("rejectReason")
        private String rejectReason;

        public int getApplicationId() { return applicationId; }
        public String getProductName() { return productName; }
        public double getLoanAmount() { return loanAmount; }
        public String getStatus() { return status; }
        public String getApplyTime() { return applyTime; }
        public String getRejectReason() { return rejectReason; }

        public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setLoanAmount(double loanAmount) { this.loanAmount = loanAmount; }
        public void setStatus(String status) { this.status = status; }
        public void setApplyTime(String applyTime) { this.applyTime = applyTime; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }
}
