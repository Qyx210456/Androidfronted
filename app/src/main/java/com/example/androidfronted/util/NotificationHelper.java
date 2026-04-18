package com.example.androidfronted.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.androidfronted.R;
import com.example.androidfronted.ui.NotificationDetailActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "message_channel";
    private static final String CHANNEL_NAME = "消息通知";
    private static final String CHANNEL_DESCRIPTION = "应用消息通知渠道";
    private static final int NOTIFICATION_ID_BASE = 2000;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESCRIPTION);
                channel.enableVibration(true);
                channel.enableLights(true);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
                
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void showNotification(int notificationId, String title, String content) {
        Intent intent = NotificationDetailActivity.newIntent(context, notificationId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setTicker(title + ": " + content);

        notificationManager.notify(NOTIFICATION_ID_BASE + notificationId, builder.build());
    }

    public void cancelNotification(int notificationId) {
        notificationManager.cancel(NOTIFICATION_ID_BASE + notificationId);
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
