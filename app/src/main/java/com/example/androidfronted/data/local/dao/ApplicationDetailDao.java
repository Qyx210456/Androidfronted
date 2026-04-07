package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.androidfronted.data.local.entity.ApplicationDetailEntity;

@Dao
public interface ApplicationDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ApplicationDetailEntity detail);

    @Query("SELECT * FROM application_details WHERE id = :id")
    ApplicationDetailEntity getById(int id);

    @Query("DELETE FROM application_details WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM application_details")
    void clearAll();
}