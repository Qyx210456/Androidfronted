package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import android.util.Log;
import com.example.androidfronted.data.model.UserInfoResponse;
import com.example.androidfronted.data.repository.UserRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PersonalInformationViewModel extends BaseViewModel {
    private final UserRepository repository;
    private final MutableLiveData<UserInfoResponse.UserData> userInfo = new MutableLiveData<>();
    private final MutableLiveData<UserInfoResponse> updateResult = new MutableLiveData<>();

    public PersonalInformationViewModel(@NonNull Application application) {
        super(application);
        this.repository = UserRepository.getInstance(application);
    }

    public MutableLiveData<UserInfoResponse.UserData> getUserInfo() {
        return userInfo;
    }

    public MutableLiveData<UserInfoResponse> getUpdateResult() {
        return updateResult;
    }

    public void loadUserInfo() {
        Log.d("PersonalInformationViewModel", "loadUserInfo called");
        repository.getUserInfo(new UserRepository.AuthCallback<UserInfoResponse>() {
            @Override
            public void onSuccess(UserInfoResponse response) {
                if (response != null && response.getCode() == 200 && response.getData() != null) {
                    userInfo.postValue(response.getData());
                }
            }

            @Override
            public void onError(String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    public void updateUserInfo(String username, File avatar) {
        Log.d("PersonalInformationViewModel", "updateUserInfo called, username: " + username);
        RequestBody avatarBody = null;
        if (avatar != null) {
            avatarBody = RequestBody.create(MediaType.parse("image/jpeg"), avatar);
        }
        repository.updateUserInfo(username, avatarBody, new UserRepository.AuthCallback<UserInfoResponse>() {
            @Override
            public void onSuccess(UserInfoResponse response) {
                updateResult.postValue(response);
                if (response != null && response.getCode() == 200 && response.getData() != null) {
                    userInfo.postValue(response.getData());
                }
            }

            @Override
            public void onError(String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    public void navigateBack() {
        navigate(NavigationEvent.NAVIGATE_BACK);
    }
}