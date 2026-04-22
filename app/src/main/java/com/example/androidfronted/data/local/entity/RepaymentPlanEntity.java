package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * 还款计划本地实体类
 * 后端不返回还款状态，由前端本地维护
 */
@Entity(tableName = "repayment_plans")
public class RepaymentPlanEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int localId;

    private int orderId;

    private int term;

    private double principal;

    private double interest;

    private double total;

    private String status;

    public RepaymentPlanEntity(int localId, int orderId, int term, double principal, 
                               double interest, double total, String status) {
        this.localId = localId;
        this.orderId = orderId;
        this.term = term;
        this.principal = principal;
        this.interest = interest;
        this.total = total;
        this.status = status;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
