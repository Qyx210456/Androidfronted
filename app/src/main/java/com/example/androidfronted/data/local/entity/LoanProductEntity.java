package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Entity(tableName = "loan_products")
public class LoanProductEntity {
    @PrimaryKey
    private int productId;
    private String productName;
    private String description;
    private String loanUsage;
    private String promotionDetails;
    private double minAmount;
    private double maxAmount;
    private String termsJson;
    private String optionsJson;

    public LoanProductEntity(int productId, String productName, String description, String loanUsage,
                          String promotionDetails, double minAmount, double maxAmount,
                          String termsJson, String optionsJson) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.loanUsage = loanUsage;
        this.promotionDetails = promotionDetails;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.termsJson = termsJson;
        this.optionsJson = optionsJson;
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
        return minAmount;
    }

    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getTermsJson() {
        return termsJson;
    }

    public void setTermsJson(String termsJson) {
        this.termsJson = termsJson;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }
}
