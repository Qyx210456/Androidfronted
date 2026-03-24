package com.example.androidfronted.data.model;

import android.util.Log;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * 贷款产品模型类
 * 对应接口 /api/loan-products/user 返回的 data 数组中的每个对象
 * 实现 Serializable 以便通过 Intent 传递
 */
public class LoanProduct implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "LoanProduct";

    @SerializedName("productId")
    private int productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("description")
    private String description;

    @SerializedName("loanUsage")
    private String loanUsage;

    @SerializedName("promotionDetails")
    private String promotionDetails;

    @SerializedName("minAmount")
    private double minAmount;

    @SerializedName("maxAmount")
    private double maxAmount;

    @SerializedName("terms")
    private List<Integer> terms;

    @SerializedName("options")
    private List<LoanOption> options;

    public int getProductId() {return productId;}

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLoanUsage() {
        return loanUsage;
    }

    public void setLoanUsage(String loanUsage) {
        this.loanUsage = loanUsage;
    }

    public String getPromotionDetails() {
        return promotionDetails;
    }

    public void setPromotionDetails(String promotionDetails) {
        this.promotionDetails = promotionDetails;
    }

    public double getMinAmount() {
        Log.d(TAG, "getMinAmount called, value: " + minAmount);
        return minAmount;
    }

    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    public double getMaxAmount() {
        Log.d(TAG, "getMaxAmount called, value: " + maxAmount);
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public List<Integer> getTerms() {
        return terms;
    }

    public void setTerms(List<Integer> terms) {
        this.terms = terms;
    }

    public List<LoanOption> getOptions() {
        return options;
    }

    public void setOptions(List<LoanOption> options) {
        this.options = options;
    }


    /**
     * 内部类：贷款方案选项
     */
    public static class LoanOption implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("optionId")
        private int optionId;

        @SerializedName("interestRate")
        private double interestRate;

        @SerializedName("loanPeriod")
        private int loanPeriod;

        @SerializedName("repaidType")
        private String repaidType;

        public int getOptionId() {
            return optionId;
        }

        public double getInterestRate() {
            return interestRate;
        }

        public int getLoanPeriod() {
            return loanPeriod;
        }

        public String getRepaidType() {
            return repaidType;
        }


    }
}