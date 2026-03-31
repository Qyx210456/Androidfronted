package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.androidfronted.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);
    
    @Update
    void update(UserEntity user);
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    UserEntity getUserById(int userId);
    
    @Query("SELECT * FROM users WHERE userName = :userName LIMIT 1")
    UserEntity getUserByUsername(String userName);
    
    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteUserById(int userId);
    
    @Query("SELECT * FROM users LIMIT 1")
    UserEntity getUser();
}
