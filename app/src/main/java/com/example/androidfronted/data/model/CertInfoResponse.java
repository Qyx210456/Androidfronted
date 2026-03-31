package com.example.androidfronted.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 查看认证信息响应体
 * 对应接口: GET /api/auth/cert-info
 */
public class CertInfoResponse {
    private int code;
    private CertInfoData data;
    private String message;

    public int getCode() { return code; }
    public CertInfoData getData() { return data; }
    public String getMessage() { return message; }

    /**
     * 数据主体类
     */
    public static class CertInfoData implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("userCert")
        private UserCert userCert;

        @SerializedName("workCert")
        private WorkCert workCert;

        @SerializedName("triCert")
        private TriCert triCert;

        @SerializedName("immovablesCert")
        private ImmovablesCert immovablesCert;

        public UserCert getUserCert() { return userCert; }
        public WorkCert getWorkCert() { return workCert; }
        public TriCert getTriCert() { return triCert; }
        public ImmovablesCert getImmovablesCert() { return immovablesCert; }
    }

    /**
     * 用户主认证信息
     */
    public static class UserCert implements Serializable {
        private static final long serialVersionUID = 1L;
        private long userId;
        private String idCard;
        private int creditScore;
        private String bankCardId;
        private int workCertId;
        private int triCertId;
        private int immovableCertId;

        public long getUserId() { return userId; }
        public String getIdCard() { return idCard; }
        public int getCreditScore() { return creditScore; }
        public String getBankCardId() { return bankCardId; }
        public int getWorkCertId() { return workCertId; }
        public int getTriCertId() { return triCertId; }
        public int getImmovableCertId() { return immovableCertId; }
    }

    /**
     * 工作认证信息 ( employment, salary )
     */
    public static class WorkCert implements Serializable {
        private static final long serialVersionUID = 1L;
        private int workCertId;
        private String employmentCertPath;
        private String salaryCertPath;

        public WorkCert() {}

        public int getWorkCertId() { return workCertId; }
        public void setWorkCertId(int workCertId) { this.workCertId = workCertId; }
        public String getEmploymentCertPath() { return employmentCertPath; }
        public void setEmploymentCertPath(String employmentCertPath) { this.employmentCertPath = employmentCertPath; }
        public String getSalaryCertPath() { return salaryCertPath; }
        public void setSalaryCertPath(String salaryCertPath) { this.salaryCertPath = salaryCertPath; }
    }

    /**
     * 第三方认证信息 ( socialSecurity, creditReport )
     */
    public static class TriCert implements Serializable {
        private static final long serialVersionUID = 1L;
        private int triCertId;
        private String socialSecurityPath;
        private String creditReportPath;

        public int getTriCertId() { return triCertId; }
        public String getSocialSecurityPath() { return socialSecurityPath; }
        public String getCreditReportPath() { return creditReportPath; }
    }

    /**
     * 不动产认证信息 ( property, car )
     */
    public static class ImmovablesCert implements Serializable {
        private static final long serialVersionUID = 1L;
        private int immovableCertId;
        private String propertyCertPath;
        private String carCertPath;
        private Object totalValue; // 文档中为 null，可能是 Double 或 String，用 Object 兼容

        public int getImmovableCertId() { return immovableCertId; }
        public String getPropertyCertPath() { return propertyCertPath; }
        public String getCarCertPath() { return carCertPath; }
        public Object getTotalValue() { return totalValue; }
    }
}