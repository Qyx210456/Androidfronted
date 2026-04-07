package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "application_details")
public class ApplicationDetailEntity {
    @PrimaryKey
    private int id;
    private int userId;
    private int productId;
    private String status;
    private double loanAmount;
    private double interestRate;
    private int loanPeriod;
    private int term;
    private String repaidType;
    private String rejectReason;
    private String applyTime;
    private String reviewTime;
    private String productName;

    public ApplicationDetailEntity(int id, int userId, int productId, String status, double loanAmount, 
                                   double interestRate, int loanPeriod, int term, String repaidType, 
                                   String rejectReason, String applyTime, String reviewTime, String productName) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.status = status;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanPeriod = loanPeriod;
        this.term = term;
        this.repaidType = repaidType;
        this.rejectReason = rejectReason;
        this.applyTime = applyTime;
        this.reviewTime = reviewTime;
        this.productName = productName;
    }

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}