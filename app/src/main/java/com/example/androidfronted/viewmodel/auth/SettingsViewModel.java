package com.example.androidfronted.viewmodel.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class SettingsViewModel extends BaseViewModel {
    private final AuthRepository authRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
    }

    public void logout() {
        authRepository.logout();
        navigate(NavigationEvent.NAVIGATE_TO_LOGIN);
    }
}
