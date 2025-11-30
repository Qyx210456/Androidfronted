package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * 贷款产品模型类
 * 对应接口 /api/loan-products/user 返回的 data 数组中的每个对象
 * 实现 Serializable 以便通过 Intent 传递
 */
public class LoanProduct implements Serializable { // 实现 Serializable

    private static final long serialVersionUID = 1L;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("description")
    private String description;

    @SerializedName("loanUsage")
    private String loanUsage;

    @SerializedName("promotionDetails")
    private String promotionDetails;

    @SerializedName("terms")
    private List<Integer> terms;

    @SerializedName("options")
    private List<LoanOption> options;

    public Long getProductId() {return productId;}

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public String getLoanUsage() {
        return loanUsage;
    }

    public String getPromotionDetails() {
        return promotionDetails;
    }

    public List<Integer> getTerms() {
        return terms;
    }

    public List<LoanOption> getOptions() {
        return options;
    }


    /**
     * 内部类：贷款方案选项
     */
    public static class LoanOption implements Serializable { // 需 Serializable
        private static final long serialVersionUID = 1L;

        @SerializedName("optionId")
        private Long optionId;

        @SerializedName("loanAmount")
        private double loanAmount;

        @SerializedName("interestRate")
        private double interestRate;

        @SerializedName("loanPeriod")
        private int loanPeriod;

        @SerializedName("repaidType")
        private String repaidType;

        public Long getOptionId() {
            return optionId;
        }

        public double getLoanAmount() {
            return loanAmount;
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