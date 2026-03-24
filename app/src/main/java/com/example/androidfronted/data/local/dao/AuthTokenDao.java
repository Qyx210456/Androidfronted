package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.androidfronted.data.local.entity.AuthTokenEntity;

@Dao
public interface AuthTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AuthTokenEntity token);
    
    @Update
    void update(AuthTokenEntity token);
    
    @Query("SELECT * FROM auth_tokens WHERE id = 1")
    AuthTokenEntity getToken();
    
    @Query("DELETE FROM auth_tokens WHERE id = 1")
    void deleteToken();
}
