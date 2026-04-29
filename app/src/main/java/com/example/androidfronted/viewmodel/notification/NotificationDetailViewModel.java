package com.example.androidfronted.viewmodel.notification;

import android.app.Application;
import android.content.Context;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.repository.NotificationRepository;
import com.example.androidfronted.util.NotificationContentFormatter;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class NotificationDetailViewModel extends BaseViewModel {
    
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<NotificationEntity> notification = new MutableLiveData<>();
    private final MutableLiveData<String> displayTitle = new MutableLiveData<>();
    private final MutableLiveData<SpannableString> formattedContent = new MutableLiveData<>();
    private final MutableLiveData<String> buttonText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isApplicationType = new MutableLiveData<>();
    private final MutableLiveData<String> formattedTime = new MutableLiveData<>();

    public NotificationDetailViewModel(@NonNull Application application) {
        super(application);
        this.notificationRepository = NotificationRepository.getInstance(application);
    }

    public void setNotification(Context context, NotificationEntity notificationEntity) {
        android.util.Log.d("NotificationDetailViewModel", "setNotification called");
        
        this.notification.setValue(notificationEntity);
        
        String title = NotificationContentFormatter.getDisplayTitle(
            notificationEntity.getTitle(), 
            notificationEntity.getBusinessType(),
            notificationEntity.getContent()
        );
        android.util.Log.d("NotificationDetailViewModel", "displayTitle: " + title);
        displayTitle.setValue(title);
        
        SpannableString content = NotificationContentFormatter.formatContent(
            context,
            notificationEntity.getBusinessType(),
            notificationEntity.getContent(),
            notificationEntity.getBusinessId()
        );
        android.util.Log.d("NotificationDetailViewModel", "formattedContent created");
        formattedContent.setValue(content);
        
        String btnText = NotificationContentFormatter.getButtonText(
            notificationEntity.getBusinessType(),
            notificationEntity.getBusinessId()
        );
        android.util.Log.d("NotificationDetailViewModel", "buttonText: " + btnText);
        buttonText.setValue(btnText);
        
        boolean isApp = NotificationContentFormatter.isApplicationRelated(
            notificationEntity.getBusinessType()
        );
        isApplicationType.setValue(isApp);
        
        String time = formatTime(notificationEntity.getCreatedAt());
        android.util.Log.d("NotificationDetailViewModel", "formattedTime: " + time);
        formattedTime.setValue(time);
    }

    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "服务通知";
        }
        String result = createdAt;
        int firstDash = result.indexOf("-");
        if (firstDash != -1) {
            result = result.substring(0, firstDash) + "年" + result.substring(firstDash + 1);
        }
        int secondDash = result.indexOf("-");
        if (secondDash != -1) {
            result = result.substring(0, secondDash) + "月" + result.substring(secondDash + 1);
        }
        int spaceIndex = result.indexOf(" ");
        if (spaceIndex != -1) {
            result = result.substring(0, spaceIndex) + "日 " + result.substring(spaceIndex + 1);
        }
        return "服务通知    " + result;
    }

    public void markAsRead(int notificationId) {
        notificationRepository.markAsRead(notificationId, new NotificationRepository.MarkAsReadCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String errorMsg) {
                showError(errorMsg);
            }
        });
    }

    public MutableLiveData<NotificationEntity> getNotification() {
        return notification;
    }

    public MutableLiveData<String> getDisplayTitle() {
        return displayTitle;
    }

    public MutableLiveData<SpannableString> getFormattedContent() {
        return formattedContent;
    }

    public MutableLiveData<String> getButtonText() {
        return buttonText;
    }

    public MutableLiveData<Boolean> getIsApplicationType() {
        return isApplicationType;
    }

    public MutableLiveData<String> getFormattedTime() {
        return formattedTime;
    }
}
