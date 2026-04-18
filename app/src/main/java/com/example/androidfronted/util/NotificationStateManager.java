package com.example.androidfronted.util;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationStateManager {
    private static final String TAG = "NotificationStateManager";
    private static NotificationStateManager instance;
    
    private boolean isInNotificationCenter = false;
    private boolean isAppInForeground = true;
    private Set<Integer> activeNotifications = new HashSet<>();
    private AtomicBoolean isInAppNotificationShowing = new AtomicBoolean(false);
    private boolean hasOfflineNotificationBeenShown = false;
    
    private NotificationStateManager() {}
    
    public static synchronized NotificationStateManager getInstance() {
        if (instance == null) {
            instance = new NotificationStateManager();
        }
        return instance;
    }
    
    public void setInNotificationCenter(boolean inNotificationCenter) {
        this.isInNotificationCenter = inNotificationCenter;
        Log.d(TAG, "isInNotificationCenter: " + inNotificationCenter);
    }
    
    public boolean isInNotificationCenter() {
        return isInNotificationCenter;
    }
    
    public void setAppInForeground(boolean inForeground) {
        this.isAppInForeground = inForeground;
        Log.d(TAG, "isAppInForeground: " + inForeground);
    }
    
    public boolean isAppInForeground() {
        return isAppInForeground;
    }
    
    public boolean tryAcquireInAppNotificationLock() {
        return isInAppNotificationShowing.compareAndSet(false, true);
    }
    
    public void releaseInAppNotificationLock() {
        isInAppNotificationShowing.set(false);
        Log.d(TAG, "In-app notification lock released");
    }
    
    public boolean hasOfflineNotificationBeenShown() {
        return hasOfflineNotificationBeenShown;
    }
    
    public void setOfflineNotificationShown(boolean shown) {
        this.hasOfflineNotificationBeenShown = shown;
        Log.d(TAG, "hasOfflineNotificationBeenShown: " + shown);
    }
    
    public void addActiveNotification(int notificationId) {
        activeNotifications.add(notificationId);
        Log.d(TAG, "Added notification: " + notificationId + ", total: " + activeNotifications.size());
    }
    
    public void removeActiveNotification(int notificationId) {
        activeNotifications.remove(notificationId);
        Log.d(TAG, "Removed notification: " + notificationId + ", total: " + activeNotifications.size());
    }
    
    public Set<Integer> getActiveNotifications() {
        return new HashSet<>(activeNotifications);
    }
    
    public void clearAllNotifications() {
        activeNotifications.clear();
        Log.d(TAG, "Cleared all notifications");
    }
    
    public void reset() {
        isInNotificationCenter = false;
        isAppInForeground = true;
        activeNotifications.clear();
        isInAppNotificationShowing.set(false);
        hasOfflineNotificationBeenShown = false;
        Log.d(TAG, "NotificationStateManager reset completed");
    }
}
