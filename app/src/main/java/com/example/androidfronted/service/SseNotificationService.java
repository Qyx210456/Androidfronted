package com.example.androidfronted.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.ui.MainActivity;
import com.example.androidfronted.util.NotificationHelper;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.util.TokenManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class SseNotificationService extends Service {
    private static final String TAG = "SseNotificationService";
    private static final String CHANNEL_ID = "foreground_service_channel";
    private static final String MESSAGE_CHANNEL_ID = "message_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1000;
    
    private final IBinder binder = new LocalBinder();
    private EventSource eventSource;
    private NotificationManager notificationManager;
    private LocalDataSource localDataSource;
    private TokenManager tokenManager;
    private NotificationHelper notificationHelper;
    private OkHttpClient client;

    public class LocalBinder extends Binder {
        public SseNotificationService getService() {
            return SseNotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");
        
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        localDataSource = new LocalDataSource(this);
        tokenManager = new TokenManager(this);
        notificationHelper = new NotificationHelper(this);
        
        createNotificationChannels();
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification());
        
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        connectToSse();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        if (eventSource != null) {
            eventSource.cancel();
        }
        super.onDestroy();
    }

    public void reconnectWithNewToken() {
        Log.d(TAG, "reconnectWithNewToken called");
        if (eventSource != null) {
            eventSource.cancel();
            Log.d(TAG, "Previous event source cancelled");
        }
        tokenManager = new TokenManager(this);
        connectToSse();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel foregroundChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "前台服务",
                    NotificationManager.IMPORTANCE_MIN
            );
            foregroundChannel.setDescription("应用正在后台运行");
            foregroundChannel.setShowBadge(false);
            foregroundChannel.enableLights(false);
            foregroundChannel.enableVibration(false);
            foregroundChannel.setSound(null, null);
            notificationManager.createNotificationChannel(foregroundChannel);
            
            NotificationChannel messageChannel = new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    "消息通知",
                    NotificationManager.IMPORTANCE_HIGH
            );
            messageChannel.setDescription("接收新消息通知");
            messageChannel.enableVibration(true);
            messageChannel.enableLights(true);
            messageChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(messageChannel);
        }
    }

    private Notification createForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_app_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setSilent(true)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void connectToSse() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty, cannot connect to SSE");
            return;
        }
        
        if (!tokenManager.isTokenValid()) {
            Log.e(TAG, "Token is expired, cannot connect to SSE");
            return;
        }
        
        String baseUrl = "http://10.0.2.2:8080/api";
        String sseUrl = baseUrl + "/notifications/stream";
        Log.d(TAG, "Connecting to SSE endpoint: " + sseUrl);
        
        Request request = new Request.Builder()
                .url(sseUrl)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "text/event-stream")
                .build();

        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                Log.d(TAG, "SSE connection opened, response code: " + response.code());
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                Log.d(TAG, "SSE event received: id=" + id + ", type=" + type + ", data=" + data);
                handleIncomingMessage(data);
            }

            @Override
            public void onClosed(EventSource eventSource) {
                Log.d(TAG, "SSE connection closed, initiating reconnect");
                reconnect();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                String errorMsg = t != null ? t.getMessage() : "unknown error";
                int responseCode = response != null ? response.code() : -1;
                Log.e(TAG, "SSE connection failed: " + errorMsg + ", response code: " + responseCode);
                reconnect();
            }
        };

        eventSource = EventSources.createFactory(client).newEventSource(request, listener);
        Log.d(TAG, "SSE event source created");
    }

    private void reconnect() {
        Log.d(TAG, "SSE reconnect scheduled in 5 seconds");
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Executing SSE reconnect");
            if (eventSource != null) {
                eventSource.cancel();
                Log.d(TAG, "Previous event source cancelled");
            }
            connectToSse();
        }, 5000);
    }
    
    private void handleIncomingMessage(String jsonData) {
        if (jsonData == null || jsonData.trim().equals("ok")) {
            Log.d(TAG, "Received heartbeat message: " + jsonData);
            return;
        }
        
        try {
            JSONObject json = new JSONObject(jsonData);
            int id = json.getInt("id");
            int userId = json.getInt("userId");
            int businessId = json.getInt("businessId");
            String businessType = json.getString("businessType");
            String title = json.getString("title");
            String content = json.getString("content");
            String createdAt = json.getString("createdAt");
            
            String token = tokenManager.getToken();
            Integer currentUserId = com.example.androidfronted.util.JWTUtils.getUserIdFromToken(token);
            
            if (currentUserId == null || userId != currentUserId) {
                Log.d(TAG, "Notification userId " + userId + " does not match current userId " + currentUserId + ", ignoring");
                return;
            }
            
            NotificationEntity notification = new NotificationEntity(
                    id, userId, businessId, businessType,
                    title, content, false, createdAt, null
            );
            
            saveNotification(notification, id, title, content);
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse notification JSON: " + e.getMessage());
        }
    }
    
    private void saveNotification(NotificationEntity notification, int id, String title, String content) {
        localDataSource.saveNotifications(Collections.singletonList(notification), 
                new LocalDataSource.DataSourceCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Log.d(TAG, "Notification saved to database");
                        handleNewNotification(notification, id, title, content);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to save notification: " + errorMessage);
                        handleNewNotification(notification, id, title, content);
                    }
                });
    }
    
    private void handleNewNotification(NotificationEntity notification, int id, String title, String content) {
        Log.d(TAG, "handleNewNotification: " + title);
        
        boolean isAppInForeground = NotificationStateManager.getInstance().isAppInForeground();
        boolean isInNotificationCenter = NotificationStateManager.getInstance().isInNotificationCenter();
        
        Log.d(TAG, "isAppInForeground: " + isAppInForeground + ", isInNotificationCenter: " + isInNotificationCenter);
        
        if (isAppInForeground) {
            EventBus.getDefault().post(new NotificationEvent.NewNotification(notification));
            Log.d(TAG, "EventBus post completed (foreground)");
        } else {
            showSystemNotification(id, title, content);
            NotificationStateManager.getInstance().addActiveNotification(id);
            Log.d(TAG, "System notification shown (background)");
        }
        
        updateUnreadCount();
    }

    private void showSystemNotification(int notificationId, String title, String content) {
        notificationHelper.showNotification(notificationId, title, content);
    }
    
    private void updateUnreadCount() {
        localDataSource.getUnreadCount(new LocalDataSource.DataSourceCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (count != null) {
                    EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(count));
                    Log.d(TAG, "UnreadCountUpdated event posted: " + count);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get unread count: " + errorMessage);
            }
        });
    }
}
