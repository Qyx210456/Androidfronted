package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.androidfronted.data.local.entity.CertificationEntity;

@Dao
public interface CertificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CertificationEntity certification);
    
    @Update
    void update(CertificationEntity certification);
    
    @Query("SELECT * FROM certifications WHERE userId = :userId")
    CertificationEntity getCertificationByUserId(int userId);
    
    @Query("DELETE FROM certifications WHERE userId = :userId")
    void deleteCertificationByUserId(int userId);
    
    @Query("DELETE FROM certifications")
    void deleteAll();
    
    @Query("UPDATE certifications SET idCard = :idCard WHERE userId = :userId")
    void updateIdCard(int userId, String idCard);
    
    @Query("UPDATE certifications SET bankCardId = :bankCardId WHERE userId = :userId")
    void updateBankCard(int userId, String bankCardId);
    
    @Query("UPDATE certifications SET workCertId = :workCertId, employmentCertPath = :employmentCertPath, salaryCertPath = :salaryCertPath WHERE userId = :userId")
    void updateWorkCert(int userId, int workCertId, String employmentCertPath, String salaryCertPath);
    
    @Query("UPDATE certifications SET immovableCertId = :immovableCertId, propertyCertPath = :propertyCertPath, carCertPath = :carCertPath WHERE userId = :userId")
    void updateImmovableCert(int userId, int immovableCertId, String propertyCertPath, String carCertPath);
    
    @Query("UPDATE certifications SET triCertId = :triCertId, socialSecurityPath = :socialSecurityPath, creditReportPath = :creditReportPath WHERE userId = :userId")
    void updateTriCert(int userId, int triCertId, String socialSecurityPath, String creditReportPath);
}
