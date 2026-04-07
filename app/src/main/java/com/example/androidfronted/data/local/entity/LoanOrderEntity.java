package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "loan_orders")
public class LoanOrderEntity implements Serializable {
    @PrimaryKey
    private int id;
    private double loanAmount;
    private String status;
    private String startTime;
    private int term;
    private int currentTerm;
    private int overdueDays;

    public LoanOrderEntity(int id, double loanAmount, String status, String startTime, int term, int currentTerm, int overdueDays) {
        this.id = id;
        this.loanAmount = loanAmount;
        this.status = status;
        this.startTime = startTime;
        this.term = term;
        this.currentTerm = currentTerm;
        this.overdueDays = overdueDays;
    }

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
