package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "loan_order_details")
public class LoanOrderDetailEntity implements Serializable {
    @PrimaryKey
    private int id;
    private int userId;
    private int productId;
    private String productName;
    private String status;
    private double repaidAmount;
    private double loanAmount;
    private double interestRate;
    private String repaidType;
    private int loanPeriod;
    private int term;
    private int currentTerm;
    private String contract;
    private int overdueDays;
    private String startTime;

    // ==== 以下为后端统计派生字段，@Ignore 不持久化（避免 DB 版本升级），离线缓存读出时为 -1 ====
    /** 应还总额（后端 Σ 所有期数 total_amount）；-1 = 后端未提供 */
    @Ignore
    private double totalAmountDue = -1;
    /** 待还总额（后端 Σ 未还期数 principal + interest）；-1 = 后端未提供 */
    @Ignore
    private double outstandingAmount = -1;
    /** 待还本金（后端 Σ 未还期数 principal）；-1 = 后端未提供 */
    @Ignore
    private double outstandingPrincipal = -1;
    /** 待还利息（后端 Σ 未还期数 interest）；-1 = 后端未提供 */
    @Ignore
    private double outstandingInterest = -1;

    public LoanOrderDetailEntity(int id, int userId, int productId, String productName, String status,
                                  double repaidAmount, double loanAmount, double interestRate,
                                  String repaidType, int loanPeriod, int term, int currentTerm,
                                  String contract, int overdueDays, String startTime) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.status = status;
        this.repaidAmount = repaidAmount;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.repaidType = repaidType;
        this.loanPeriod = loanPeriod;
        this.term = term;
        this.currentTerm = currentTerm;
        this.contract = contract;
        this.overdueDays = overdueDays;
        this.startTime = startTime;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public double getTotalAmountDue() {
        return totalAmountDue;
    }

    public void setTotalAmountDue(double totalAmountDue) {
        this.totalAmountDue = totalAmountDue;
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public double getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(double outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public double getOutstandingInterest() {
        return outstandingInterest;
    }

    public void setOutstandingInterest(double outstandingInterest) {
        this.outstandingInterest = outstandingInterest;
    }
}
