package com.example.androidfronted.viewmodel.notification;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.repository.NotificationRepository;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class NotificationViewModel extends BaseViewModel {
    private static final String TAG = "NotificationViewModel";
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<List<NotificationEntity>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    private final MutableLiveData<NotificationEntity> newNotification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public NotificationViewModel(@NonNull Application application, NotificationRepository notificationRepository) {
        super(application);
        this.notificationRepository = notificationRepository;
        EventBus.getDefault().register(this);
        Log.d(TAG, "NotificationViewModel created, EventBus registered");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        EventBus.getDefault().unregister(this);
    }

    public MutableLiveData<List<NotificationEntity>> getNotifications() {
        return notifications;
    }

    public MutableLiveData<Integer> getUnreadCount() {
        return unreadCount;
    }
    
    public MutableLiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }

    public MutableLiveData<NotificationEntity> getNewNotification() {
        return newNotification;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNotification(NotificationEvent.NewNotification event) {
        Log.d(TAG, "Received NewNotification event: " + event.getNotification().getTitle());
        NotificationEntity notification = event.getNotification();
        newNotification.setValue(null);
        newNotification.setValue(notification);
        incrementUnreadCount();
        loadNotificationsFromLocal();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadCountUpdated(NotificationEvent.UnreadCountUpdated event) {
        Log.d(TAG, "Received UnreadCountUpdated event: " + event.getCount());
        unreadCount.setValue(event.getCount());
    }

    public void updateUnreadCount(int count) {
        unreadCount.setValue(count);
    }

    public void incrementUnreadCount() {
        Integer current = unreadCount.getValue();
        if (current == null) current = 0;
        unreadCount.setValue(current + 1);
        Log.d(TAG, "Unread count incremented to: " + (current + 1));
    }

    public void decrementUnreadCount() {
        Integer current = unreadCount.getValue();
        if (current == null || current <= 0) {
            unreadCount.setValue(0);
        } else {
            unreadCount.setValue(current - 1);
            Log.d(TAG, "Unread count decremented to: " + (current - 1));
        }
    }

    public void loadNotifications() {
        showLoading();
        notificationRepository.getNotifications(new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(List<NotificationEntity> notificationList) {
                hideLoading();
                notifications.postValue(notificationList);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public void loadNotificationsFromLocal() {
        notificationRepository.getNotificationsFromLocal(new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(List<NotificationEntity> notificationList) {
                notifications.postValue(notificationList);
            }

            @Override
            public void onError(String errorMsg) {
                showError(errorMsg);
            }
        });
    }

    public void markAsRead(int notificationId) {
        notificationRepository.markAsRead(notificationId, new NotificationRepository.MarkAsReadCallback() {
            @Override
            public void onSuccess(String message) {
                decrementUnreadCount();
                loadNotificationsFromLocal();
                loadUnreadCountAndNotify();
            }

            @Override
            public void onError(String errorMsg) {
                errorMessage.postValue(errorMsg);
            }
        });
    }

    public void markAllAsRead() {
        notificationRepository.markAllAsRead(new NotificationRepository.MarkAsReadCallback() {
            @Override
            public void onSuccess(String message) {
                successMessage.postValue(message);
                unreadCount.setValue(0);
                loadNotificationsFromLocal();
                EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(0));
            }

            @Override
            public void onError(String errorMsg) {
                errorMessage.postValue(errorMsg);
            }
        });
    }
    
    private void loadUnreadCountAndNotify() {
        notificationRepository.getUnreadCount(new NotificationRepository.UnreadCountCallback() {
            @Override
            public void onSuccess(int count) {
                unreadCount.postValue(count);
                EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(count));
            }

            @Override
            public void onError(String errorMsg) {
                unreadCount.postValue(0);
                EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(0));
            }
        });
    }

    public void loadUnreadCount() {
        notificationRepository.getUnreadCount(new NotificationRepository.UnreadCountCallback() {
            @Override
            public void onSuccess(int count) {
                unreadCount.postValue(count);
            }

            @Override
            public void onError(String errorMsg) {
                unreadCount.postValue(0);
            }
        });
    }

    protected void showLoading() {
        isLoading.postValue(true);
    }

    protected void hideLoading() {
        isLoading.postValue(false);
    }

    protected void showError(String message) {
        errorMessage.postValue(message);
    }
}
