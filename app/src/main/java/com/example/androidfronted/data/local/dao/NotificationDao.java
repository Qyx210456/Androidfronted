package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidfronted.data.local.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    void insert(NotificationEntity notification);

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    void insertAll(List<NotificationEntity> notifications);

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    List<NotificationEntity> getAllNotifications();

    @Query("SELECT * FROM notifications WHERE id = :id")
    NotificationEntity getById(int id);

    @Query("SELECT COUNT(*) FROM notifications WHERE readFlag = 0")
    int getUnreadCount();

    @Query("SELECT * FROM notifications WHERE readFlag = 0 ORDER BY createdAt DESC")
    List<NotificationEntity> getUnreadNotifications();

    @Query("UPDATE notifications SET readFlag = 1, readAt = :readAt WHERE id = :id")
    void markAsRead(int id, String readAt);

    @Query("UPDATE notifications SET readFlag = 1, readAt = :readAt WHERE readFlag = 0")
    void markAllAsRead(String readAt);

    @Query("DELETE FROM notifications")
    void deleteAll();

    @Query("DELETE FROM notifications WHERE id = :id")
    void deleteById(int id);
}
