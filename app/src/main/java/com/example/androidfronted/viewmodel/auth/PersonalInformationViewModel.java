package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import android.util.Log;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.model.UserInfoResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.data.repository.UserRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PersonalInformationViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final MutableLiveData<UserInfoResponse.UserData> userInfo = new MutableLiveData<>();
    private final MutableLiveData<UserInfoResponse> updateResult = new MutableLiveData<>();
    private final MutableLiveData<CertState> certificationState = new MutableLiveData<>();
    private final MutableLiveData<Integer> creditScore = new MutableLiveData<>();

    public PersonalInformationViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = UserRepository.getInstance(application);
        this.authRepository = AuthRepository.getInstance(application);
    }

    public MutableLiveData<UserInfoResponse.UserData> getUserInfo() {
        return userInfo;
    }

    public MutableLiveData<UserInfoResponse> getUpdateResult() {
        return updateResult;
    }

    public MutableLiveData<CertState> getCertificationState() {
        return certificationState;
    }

    public MutableLiveData<Integer> getCreditScore() {
        return creditScore;
    }

    public void loadUserInfo() {
        Log.d("PersonalInformationViewModel", "loadUserInfo called");
        userRepository.getUserInfo(new UserRepository.AuthCallback<UserInfoResponse>() {
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

    public void loadCertificationStatus() {
        Log.d("PersonalInformationViewModel", "loadCertificationStatus called");
        
        authRepository.getLocalCertInfo(new AuthRepository.AuthCallback<com.example.androidfronted.data.local.entity.CertificationEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.CertificationEntity certification) {
                if (certification != null && certification.getCreditScore() > 0) {
                    creditScore.postValue(certification.getCreditScore());
                    Log.d("PersonalInformationViewModel", "Loaded local credit score: " + certification.getCreditScore());
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
        
        authRepository.getLocalIdCertState(new AuthRepository.AuthCallback<CertState>() {
            @Override
            public void onSuccess(CertState certState) {
                certificationState.postValue(certState);
                authRepository.getCertInfo(new AuthRepository.AuthCallback<com.example.androidfronted.data.model.CertInfoResponse>() {
                    @Override
                    public void onSuccess(com.example.androidfronted.data.model.CertInfoResponse response) {
                        if (response != null && response.getData() != null && response.getData().getUserCert() != null) {
                            int score = response.getData().getUserCert().getCreditScore();
                            creditScore.postValue(score);
                        }
                        authRepository.getLocalIdCertState(new AuthRepository.AuthCallback<CertState>() {
                            @Override
                            public void onSuccess(CertState updatedCertState) {
                                certificationState.postValue(updatedCertState);
                            }

                            @Override
                            public void onError(String errorMessage) {
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                certificationState.postValue(null);
            }
        });
    }

    public void updateUserInfo(String username, File avatar) {
        Log.d("PersonalInformationViewModel", "updateUserInfo called, username: " + username);
        RequestBody avatarBody = null;
        if (avatar != null) {
            avatarBody = RequestBody.create(MediaType.parse("image/jpeg"), avatar);
        }
        userRepository.updateUserInfo(username, avatarBody, new UserRepository.AuthCallback<UserInfoResponse>() {
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