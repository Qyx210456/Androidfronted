package com.example.androidfronted.viewmodel.auth;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.service.SseNotificationService;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class SettingsViewModel extends BaseViewModel {
    private final AuthRepository authRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
    }

    public void logout() {
        Intent serviceIntent = new Intent(getApplication(), SseNotificationService.class);
        getApplication().stopService(serviceIntent);
        
        NotificationStateManager.getInstance().reset();
        
        authRepository.logout();
        navigate(NavigationEvent.NAVIGATE_TO_LOGIN);
    }
}
