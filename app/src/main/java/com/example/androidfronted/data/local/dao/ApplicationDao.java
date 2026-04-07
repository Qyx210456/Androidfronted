package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import java.util.List;

@Dao
public interface ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ApplicationEntity> applications);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ApplicationEntity application);

    @Query("SELECT * FROM applications ORDER BY applyTime DESC")
    List<ApplicationEntity> getAllApplications();

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY applyTime DESC")
    List<ApplicationEntity> getApplicationsByStatus(String status);

    @Query("SELECT * FROM applications WHERE applicationId = :applicationId")
    ApplicationEntity getApplicationById(int applicationId);

    @Query("DELETE FROM applications")
    void deleteAll();

    @Query("DELETE FROM applications WHERE applicationId = :applicationId")
    void deleteApplicationById(int applicationId);

    @Query("UPDATE applications SET status = :status WHERE applicationId = :applicationId")
    void updateStatus(int applicationId, String status);
}
