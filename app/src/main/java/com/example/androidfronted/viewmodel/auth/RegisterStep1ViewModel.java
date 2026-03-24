package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class RegisterStep1ViewModel extends BaseViewModel {
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";

    public RegisterStep1ViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getUsername() {
        return username;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public MutableLiveData<String> getConfirmPassword() {
        return confirmPassword;
    }

    public MutableLiveData<String> getValidationError() {
        return validationError;
    }

    public void setUsername(String value) {
        username.setValue(value);
    }

    public void setPassword(String value) {
        password.setValue(value);
    }

    public void setConfirmPassword(String value) {
        confirmPassword.setValue(value);
    }

    private boolean validate() {
        String usernameValue = username.getValue();
        String passwordValue = password.getValue();
        String confirmValue = confirmPassword.getValue();

        if (usernameValue == null || usernameValue.trim().isEmpty()) {
            validationError.setValue("请输入用户名");
            return false;
        }

        if (passwordValue == null || passwordValue.trim().isEmpty()) {
            validationError.setValue("请输入密码");
            return false;
        }

        if (!passwordValue.matches(PASSWORD_PATTERN)) {
            validationError.setValue("密码需包含大小写字母、数字和特殊字符（如!@#$%），长度8-20位");
            return false;
        }

        if (confirmValue == null || confirmValue.trim().isEmpty()) {
            validationError.setValue("请再次输入密码");
            return false;
        }

        if (!passwordValue.equals(confirmValue)) {
            validationError.setValue("两次密码不一致");
            return false;
        }

        validationError.setValue(null);
        return true;
    }

    public void navigateToStep2() {
        if (!validate()) {
            return;
        }

        String usernameValue = username.getValue();
        String passwordValue = password.getValue();

        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_REGISTER_STEP_2, 
                new RegisterData(usernameValue.trim(), passwordValue.trim()));
    }

    public void navigateToLogin() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_LOGIN);
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }

    public static class RegisterData {
        public final String username;
        public final String password;

        public RegisterData(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}