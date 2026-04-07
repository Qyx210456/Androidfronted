package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "applications")
public class ApplicationEntity {
    @PrimaryKey
    private int applicationId;
    private String productName;
    private double loanAmount;
    private String status;
    private String applyTime;
    private String rejectReason;

    public ApplicationEntity(int applicationId, String productName, double loanAmount,
                             String status, String applyTime, String rejectReason) {
        this.applicationId = applicationId;
        this.productName = productName;
        this.loanAmount = loanAmount;
        this.status = status;
        this.applyTime = applyTime;
        this.rejectReason = rejectReason;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(double loanAmount) { this.loanAmount = loanAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApplyTime() { return applyTime; }
    public void setApplyTime(String applyTime) { this.applyTime = applyTime; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
