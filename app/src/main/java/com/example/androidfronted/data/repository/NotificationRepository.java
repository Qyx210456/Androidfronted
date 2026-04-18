package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.model.NotificationResponse;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;
import com.example.androidfronted.util.TokenManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationRepository {
    private static final String TAG = "NotificationRepo";
    private static NotificationRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final TokenManager tokenManager;

    private NotificationRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
        this.tokenManager = new TokenManager(context);
    }

    public static synchronized NotificationRepository getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getNotifications(NotificationCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getNotifications, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        
        remoteDataSource.getMyNotifications(token, new RemoteDataSource.NetworkCallback<NotificationResponse>() {
            @Override
            public void onSuccess(NotificationResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got " + response.getData().size() + " notifications");
                    
                    List<NotificationEntity> entities = new ArrayList<>();
                    for (NotificationResponse.NotificationData data : response.getData()) {
                        Log.d(TAG, "Parsing notification: id=" + data.getId() + ", type=" + data.getBusinessType() + ", title=" + data.getTitle() + ", readFlag=" + data.isReadFlag());
                        
                        NotificationEntity entity = new NotificationEntity(
                            data.getId(),
                            data.getUserId(),
                            data.getBusinessId(),
                            data.getBusinessType(),
                            data.getTitle(),
                            data.getContent(),
                            data.isReadFlag(),
                            data.getCreatedAt(),
                            data.getReadAt()
                        );
                        entities.add(entity);
                    }
                    
                    localDataSource.clearNotifications(new LocalDataSource.DataSourceCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Log.d(TAG, "Old notifications cleared");
                            saveNewNotifications(entities, callback);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Failed to clear old notifications: " + errorMessage);
                            saveNewNotifications(entities, callback);
                        }
                    });
                } else {
                    Log.e(TAG, "Response or data is null");
                    callback.onError("获取通知失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get notifications: " + errorMessage);
                localDataSource.getAllNotifications(new LocalDataSource.DataSourceCallback<List<NotificationEntity>>() {
                    @Override
                    public void onSuccess(List<NotificationEntity> data) {
                        if (data != null && !data.isEmpty()) {
                            callback.onSuccess(data);
                        } else {
                            callback.onError(errorMessage);
                        }
                    }

                    @Override
                    public void onError(String localError) {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }
    
    private void saveNewNotifications(List<NotificationEntity> entities, NotificationCallback callback) {
        localDataSource.saveNotifications(entities, new LocalDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Notifications saved to database successfully");
                callback.onSuccess(entities);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to save notifications to database: " + errorMessage);
                callback.onSuccess(entities);
            }
        });
    }

    public void getNotificationsFromLocal(NotificationCallback callback) {
        localDataSource.getAllNotifications(new LocalDataSource.DataSourceCallback<List<NotificationEntity>>() {
            @Override
            public void onSuccess(List<NotificationEntity> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void fetchOfflineNotifications(OfflineNotificationCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "fetchOfflineNotifications, token: " + (token != null ? "not null" : "null"));
        
        remoteDataSource.getMyNotifications(token, new RemoteDataSource.NetworkCallback<NotificationResponse>() {
            @Override
            public void onSuccess(NotificationResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "fetchOfflineNotifications: Got " + response.getData().size() + " notifications");
                    
                    List<NotificationEntity> entities = new ArrayList<>();
                    for (NotificationResponse.NotificationData data : response.getData()) {
                        NotificationEntity entity = new NotificationEntity(
                            data.getId(),
                            data.getUserId(),
                            data.getBusinessId(),
                            data.getBusinessType(),
                            data.getTitle(),
                            data.getContent(),
                            data.isReadFlag(),
                            data.getCreatedAt(),
                            data.getReadAt()
                        );
                        entities.add(entity);
                    }
                    
                    localDataSource.clearNotifications(new LocalDataSource.DataSourceCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Log.d(TAG, "Old notifications cleared for offline fetch");
                            saveOfflineNotifications(entities, callback);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Failed to clear old notifications: " + errorMessage);
                            saveOfflineNotifications(entities, callback);
                        }
                    });
                } else {
                    Log.d(TAG, "fetchOfflineNotifications: No notifications");
                    callback.onSuccess(0, null);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "fetchOfflineNotifications failed: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    private void saveOfflineNotifications(List<NotificationEntity> entities, OfflineNotificationCallback callback) {
        localDataSource.saveNotifications(entities, new LocalDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Offline notifications saved to database");
                processOfflineNotificationResult(entities, callback);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to save offline notifications: " + errorMessage);
                processOfflineNotificationResult(entities, callback);
            }
        });
    }
    
    private void processOfflineNotificationResult(List<NotificationEntity> entities, OfflineNotificationCallback callback) {
        Collections.sort(entities, (n1, n2) -> {
            if (n1.getCreatedAt() == null && n2.getCreatedAt() == null) return 0;
            if (n1.getCreatedAt() == null) return 1;
            if (n2.getCreatedAt() == null) return -1;
            return n2.getCreatedAt().compareTo(n1.getCreatedAt());
        });
        
        int unreadCount = 0;
        NotificationEntity latestUnread = null;
        
        for (NotificationEntity entity : entities) {
            if (!entity.isReadFlag()) {
                unreadCount++;
                if (latestUnread == null) {
                    latestUnread = entity;
                }
            }
        }
        
        Log.d(TAG, "Offline notifications processed: unreadCount=" + unreadCount + ", latestUnread=" + (latestUnread != null ? latestUnread.getTitle() : "null"));
        callback.onSuccess(unreadCount, latestUnread);
    }

    public void markAsRead(int notificationId, MarkAsReadCallback callback) {
        String token = tokenManager.getToken();
        String readAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        remoteDataSource.markNotificationAsRead(token, notificationId, new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String response) {
                localDataSource.markNotificationAsRead(notificationId, readAt, new LocalDataSource.DataSourceCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        callback.onSuccess("已标记为已读");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onSuccess("已标记为已读");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to mark notification as read: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void markAllAsRead(MarkAsReadCallback callback) {
        String token = tokenManager.getToken();
        String readAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        localDataSource.getUnreadNotifications(new LocalDataSource.DataSourceCallback<List<NotificationEntity>>() {
            @Override
            public void onSuccess(List<NotificationEntity> unreadNotifications) {
                if (unreadNotifications.isEmpty()) {
                    localDataSource.markAllNotificationsAsRead(readAt, new LocalDataSource.DataSourceCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            callback.onSuccess("已标记所有通知为已读");
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError(errorMessage);
                        }
                    });
                } else {
                    markNotificationsAsRead(unreadNotifications, 0, readAt, callback);
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private void markNotificationsAsRead(List<NotificationEntity> notifications, int index, String readAt, MarkAsReadCallback callback) {
        if (index >= notifications.size()) {
            localDataSource.markAllNotificationsAsRead(readAt, new LocalDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    callback.onSuccess("已标记所有通知为已读");
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
            return;
        }

        NotificationEntity notification = notifications.get(index);
        String token = tokenManager.getToken();
        
        remoteDataSource.markNotificationAsRead(token, notification.getId(), new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String response) {
                markNotificationsAsRead(notifications, index + 1, readAt, callback);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to mark notification " + notification.getId() + " as read: " + errorMessage);
                markNotificationsAsRead(notifications, index + 1, readAt, callback);
            }
        });
    }

    public void getUnreadCount(UnreadCountCallback callback) {
        localDataSource.getUnreadCount(new LocalDataSource.DataSourceCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                if (callback != null) {
                    callback.onSuccess(data);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    public interface NotificationCallback {
        void onSuccess(List<NotificationEntity> notifications);
        void onError(String errorMessage);
    }

    public interface MarkAsReadCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface UnreadCountCallback {
        void onSuccess(int count);
        void onError(String errorMessage);
    }

    public interface OfflineNotificationCallback {
        void onSuccess(int unreadCount, NotificationEntity latestUnread);
        void onError(String errorMessage);
    }
}
