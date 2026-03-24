package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class VerifyCodeLoginViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();

    public VerifyCodeLoginViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
    }

    public MutableLiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public void loginWithVerifyCode(String phone, String verifyCode) {
        showLoading();
        repository.login(new com.example.androidfronted.data.model.LoginRequest(phone, verifyCode),
                new AuthRepository.AuthCallback<LoginResponse>() {
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
