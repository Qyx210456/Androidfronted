package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class RegisterStep2ViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    private final MutableLiveData<String> verifyCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> agreementChecked = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();

    private String username;
    private String password;
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    public RegisterStep2ViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
        agreementChecked.setValue(false);
    }

    public MutableLiveData<String> getPhone() {
        return phone;
    }

    public MutableLiveData<String> getVerifyCode() {
        return verifyCode;
    }

    public MutableLiveData<Boolean> getAgreementChecked() {
        return agreementChecked;
    }

    public MutableLiveData<String> getValidationError() {
        return validationError;
    }

    public MutableLiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String value) {
        phone.setValue(value);
    }

    public void setVerifyCode(String value) {
        verifyCode.setValue(value);
    }

    public void setAgreementChecked(boolean checked) {
        agreementChecked.setValue(checked);
    }

    private boolean validate() {
        String phoneValue = phone.getValue();
        Boolean checked = agreementChecked.getValue();

        if (phoneValue == null || phoneValue.trim().isEmpty()) {
            validationError.setValue("请输入手机号");
            return false;
        }

        if (!phoneValue.matches(PHONE_PATTERN)) {
            validationError.setValue("请输入有效的中国大陆手机号");
            return false;
        }

        if (checked == null || !checked) {
            validationError.setValue("请同意《服务条款》和《隐私政策》");
            return false;
        }

        validationError.setValue(null);
        return true;
    }

    public void register() {
        if (!validate()) {
            return;
        }

        String phoneValue = phone.getValue();
        Boolean checked = agreementChecked.getValue();

        if (username == null || password == null) {
            validationError.setValue("注册信息缺失");
            return;
        }

        RegisterRequest request = new RegisterRequest(username, phoneValue.trim(), password);
        showLoading();
        repository.register(request, new AuthRepository.AuthCallback<>() {
            @Override
            public void onSuccess(RegisterResponse response) {
                hideLoading();
                registerSuccess.postValue(true);
                navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_LOGIN);
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                if ("该手机号已被注册".equals(errorMessage)) {
                    validationError.postValue("该手机号已被注册");
                } else {
                    validationError.postValue("注册失败: " + errorMessage);
                }
                registerSuccess.postValue(false);
            }
        });
    }

    public void navigateBackToStep1() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }
}