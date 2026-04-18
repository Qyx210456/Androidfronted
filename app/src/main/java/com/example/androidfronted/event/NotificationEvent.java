package com.example.androidfronted.event;

import com.example.androidfronted.data.local.entity.NotificationEntity;

public class NotificationEvent {
    public static class NewNotification {
        private final NotificationEntity notification;

        public NewNotification(NotificationEntity notification) {
            this.notification = notification;
        }

        public NotificationEntity getNotification() {
            return notification;
        }
    }

    public static class UnreadCountUpdated {
        private final int count;

        public UnreadCountUpdated(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    public static class MarkAsRead {
        private final int notificationId;

        public MarkAsRead(int notificationId) {
            this.notificationId = notificationId;
        }

        public int getNotificationId() {
            return notificationId;
        }
    }

    public static class OfflineNotificationSummary {
        private final int count;
        private final NotificationEntity latestNotification;

        public OfflineNotificationSummary(int count, NotificationEntity latestNotification) {
            this.count = count;
            this.latestNotification = latestNotification;
        }

        public int getCount() {
            return count;
        }

        public NotificationEntity getLatestNotification() {
            return latestNotification;
        }
    }
}
