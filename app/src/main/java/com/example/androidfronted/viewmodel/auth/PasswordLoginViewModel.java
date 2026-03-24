package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class PasswordLoginViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();

    public PasswordLoginViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
    }

    public MutableLiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public void login(LoginRequest request) {
        showLoading();
        repository.login(request, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();
                loginResult.postValue(response);
                navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_HOME);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }
}
