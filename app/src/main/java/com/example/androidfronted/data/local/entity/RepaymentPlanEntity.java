package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "repayment_plans")
public class RepaymentPlanEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int localId;

    private long planId;

    private long orderId;

    private int term;

    private double principal;

    private double interest;

    private double totalAmount;

    private String status;

    private double remainingPrincipal;

    private double remainingInterest;

    private String dueDate;

    private String actualPayDate;

    public RepaymentPlanEntity(int localId, long planId, long orderId, int term, double principal,
                               double interest, double totalAmount, String status,
                               double remainingPrincipal, double remainingInterest,
                               String dueDate, String actualPayDate) {
        this.localId = localId;
        this.planId = planId;
        this.orderId = orderId;
        this.term = term;
        this.principal = principal;
        this.interest = interest;
        this.totalAmount = totalAmount;
        this.status = status;
        this.remainingPrincipal = remainingPrincipal;
        this.remainingInterest = remainingInterest;
        this.dueDate = dueDate;
        this.actualPayDate = actualPayDate;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
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
}
