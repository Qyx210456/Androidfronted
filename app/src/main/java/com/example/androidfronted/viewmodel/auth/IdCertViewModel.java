package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import android.util.Log;
import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class IdCertViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthSubmitResponse> submitResult = new MutableLiveData<>();
    private final MutableLiveData<CertState> certState = new MutableLiveData<>();
    private final MutableLiveData<CertInfoResponse.UserCert> certData = new MutableLiveData<>();
    private CertState previousState = null;

    public IdCertViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
        // 在构造函数中加载本地认证信息
        loadLocalCertInfo();
    }

    public MutableLiveData<AuthSubmitResponse> getSubmitResult() {
        return submitResult;
    }

    public MutableLiveData<CertState> getCertState() {
        return certState;
    }

    public MutableLiveData<CertInfoResponse.UserCert> getCertData() {
        return certData;
    }

    public interface CertInfoCallback {
        void onSuccess(CertInfoResponse response);
        void onError(String errorMessage);
    }

    public void getCertInfo(CertInfoCallback callback) {
        CertState currentStateBeforeRequest = certState.getValue();
        Log.d("IdCertViewModel", "getCertInfo called, currentStateBeforeRequest: " + currentStateBeforeRequest);
        
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                CertState currentStateAfterRequest = certState.getValue();
                Log.d("IdCertViewModel", "getCertInfo onSuccess, currentStateAfterRequest: " + currentStateAfterRequest);
                
                if (response != null && response.getData() != null) {
                    CertInfoResponse.UserCert userCert = response.getData().getUserCert();
                    certData.postValue(userCert);
                    
                    if (currentStateAfterRequest != CertState.UPLOADING) {
                        if (userCert != null && userCert.getIdCard() != null && !userCert.getIdCard().isEmpty()) {
                            Log.d("IdCertViewModel", "getCertInfo, setting state to CERTIFIED");
                            certState.postValue(CertState.CERTIFIED);
                        } else {
                            Log.d("IdCertViewModel", "getCertInfo, idCard is null or empty, keeping current state");
                        }
                    } else {
                        Log.d("IdCertViewModel", "getCertInfo, current state is UPLOADING, not changing state");
                    }
                } else {
                    Log.d("IdCertViewModel", "getCertInfo, response is null or data is null, keeping current state");
                }
                
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(String errorMsg) {
                CertState currentStateAfterRequest = certState.getValue();
                Log.d("IdCertViewModel", "getCertInfo onError, currentStateAfterRequest: " + currentStateAfterRequest);
                
                // 网络请求失败时，保持当前状态，不设置为 NOT_CERTIFIED
                // 这样可以避免覆盖本地的已认证状态，防止页面闪烁
                Log.d("IdCertViewModel", "getCertInfo onError, keeping current state");
                
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            }
        });
    }

    public void submitCert(String idCard) {
        Log.d("IdCertViewModel", "submitCert called, idCard: " + idCard);
        certState.setValue(CertState.UPLOADING);
        Log.d("IdCertViewModel", "submitCert, state changed to UPLOADING");
        repository.submitBasicCert(idCard,
                new AuthRepository.AuthCallback<AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(AuthSubmitResponse response) {
                        Log.d("IdCertViewModel", "submitCert, onSuccess");
                        submitResult.postValue(response);
                        certState.postValue(CertState.CERTIFIED);
                        Log.d("IdCertViewModel", "submitCert, state changed to CERTIFIED");
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e("IdCertViewModel", "submitCert, onError: " + errorMsg);
                        certState.postValue(CertState.NOT_CERTIFIED);
                        Log.d("IdCertViewModel", "submitCert, state changed to NOT_CERTIFIED");
                        showError(errorMsg);
                    }
                });
    }

    public void startUpload() {
        previousState = certState.getValue();
        certState.setValue(CertState.UPLOADING);
    }

    public void cancelUpload() {
        certState.postValue(previousState);
    }

    public void navigateToUpload() {
        navigate(NavigationEvent.NAVIGATE_TO_ID_CERT_UPLOAD);
    }

    public void navigateBack() {
        navigate(NavigationEvent.NAVIGATE_BACK);
    }
    
    private void loadLocalCertInfo() {
        Log.d("IdCertViewModel", "loadLocalCertInfo called");
        repository.getLocalIdCertState(new AuthRepository.AuthCallback<CertState>() {
            @Override
            public void onSuccess(CertState certStateValue) {
                Log.d("IdCertViewModel", "loadLocalCertInfo, onSuccess, certState: " + certStateValue);
                certState.postValue(certStateValue);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("IdCertViewModel", "loadLocalCertInfo, onError: " + errorMessage);
            }
        });
    }
}
