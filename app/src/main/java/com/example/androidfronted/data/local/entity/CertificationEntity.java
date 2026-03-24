package com.example.androidfronted.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "certifications")
public class CertificationEntity {
    @PrimaryKey
    private int userId;
    private String idCard;
    private int creditScore;
    private String bankCardId;
    private int workCertId;
    private String employmentCertPath;
    private String salaryCertPath;
    private int triCertId;
    private String socialSecurityPath;
    private String creditReportPath;
    private int immovableCertId;
    private String propertyCertPath;
    private String carCertPath;

    public CertificationEntity(int userId, String idCard, int creditScore, String bankCardId,
                           int workCertId, String employmentCertPath, String salaryCertPath,
                           int triCertId, String socialSecurityPath, String creditReportPath,
                           int immovableCertId, String propertyCertPath, String carCertPath) {
        this.userId = userId;
        this.idCard = idCard;
        this.creditScore = creditScore;
        this.bankCardId = bankCardId;
        this.workCertId = workCertId;
        this.employmentCertPath = employmentCertPath;
        this.salaryCertPath = salaryCertPath;
        this.triCertId = triCertId;
        this.socialSecurityPath = socialSecurityPath;
        this.creditReportPath = creditReportPath;
        this.immovableCertId = immovableCertId;
        this.propertyCertPath = propertyCertPath;
        this.carCertPath = carCertPath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public String getBankCardId() {
        return bankCardId;
    }

    public void setBankCardId(String bankCardId) {
        this.bankCardId = bankCardId;
    }

    public int getWorkCertId() {
        return workCertId;
    }

    public void setWorkCertId(int workCertId) {
        this.workCertId = workCertId;
    }

    public String getEmploymentCertPath() {
        return employmentCertPath;
    }

    public void setEmploymentCertPath(String employmentCertPath) {
        this.employmentCertPath = employmentCertPath;
    }

    public String getSalaryCertPath() {
        return salaryCertPath;
    }

    public void setSalaryCertPath(String salaryCertPath) {
        this.salaryCertPath = salaryCertPath;
    }

    public int getTriCertId() {
        return triCertId;
    }

    public void setTriCertId(int triCertId) {
        this.triCertId = triCertId;
    }

    public String getSocialSecurityPath() {
        return socialSecurityPath;
    }

    public void setSocialSecurityPath(String socialSecurityPath) {
        this.socialSecurityPath = socialSecurityPath;
    }

    public String getCreditReportPath() {
        return creditReportPath;
    }

    public void setCreditReportPath(String creditReportPath) {
        this.creditReportPath = creditReportPath;
    }

    public int getImmovableCertId() {
        return immovableCertId;
    }

    public void setImmovableCertId(int immovableCertId) {
        this.immovableCertId = immovableCertId;
    }

    public String getPropertyCertPath() {
        return propertyCertPath;
    }

    public void setPropertyCertPath(String propertyCertPath) {
        this.propertyCertPath = propertyCertPath;
    }

    public String getCarCertPath() {
        return carCertPath;
    }

    public void setCarCertPath(String carCertPath) {
        this.carCertPath = carCertPath;
    }
}
