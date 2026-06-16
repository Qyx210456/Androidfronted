package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import android.app.Application;
import android.util.Log;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.AuthRepository;

public class PropertyCertViewModel extends BaseCertViewModel<CertInfoResponse.ImmovablesCert> {

    public PropertyCertViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void getCertInfo(CertInfoCallback callback) {
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                if (response != null && response.getData() != null) {
                    CertInfoResponse.ImmovablesCert immovablesCert = response.getData().getImmovablesCert();
                    CertInfoResponse.UserCert userCert = response.getData().getUserCert();
                    
                    certData.postValue(immovablesCert);
                    
                    if (userCert != null && userCert.getBankCardId() != null) {
                        bankCardId.postValue(userCert.getBankCardId());
                    }
                    
                    if (immovablesCert != null && 
                        (immovablesCert.getPropertyCertPath() != null && !immovablesCert.getPropertyCertPath().isEmpty() ||
                         immovablesCert.getCarCertPath() != null && !immovablesCert.getCarCertPath().isEmpty())) {
                        CertState currentState = certState.getValue();
                        if (currentState != CertState.UPLOADING && currentState != null) {
                            certState.postValue(CertState.CERTIFIED);
                        }
                    } else {
                        Log.d("PropertyCertViewModel", "getCertInfo, immovablesCert is null or empty, keeping current state");
                    }
                }
                
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            }
        });
    }

    @Override
    public void submitCert(okhttp3.RequestBody propertyFile, okhttp3.RequestBody carFile) {
        if (previousState == null) {
            previousState = certState.getValue();
        }
        certState.setValue(CertState.UPLOADING);
        repository.submitOtherCert("", propertyFile, carFile, null, null, null, null,
                new AuthRepository.AuthCallback<com.example.androidfronted.data.model.AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(com.example.androidfronted.data.model.AuthSubmitResponse response) {
                        handleSuccess(response);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        handleError(errorMsg);
                    }
                });
    }

    public void navigateToUpload() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_PROPERTY_CERT_UPLOAD);
    }

    @Override
    protected void loadLocalCertInfo() {
        Log.d("PropertyCertViewModel", "loadLocalCertInfo called");
        repository.getLocalPropertyCertState(new AuthRepository.AuthCallback<CertState>() {
            @Override
            public void onSuccess(CertState certStateValue) {
                Log.d("PropertyCertViewModel", "loadLocalCertInfo, onSuccess, certState: " + certStateValue);
                certState.postValue(certStateValue);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("PropertyCertViewModel", "loadLocalCertInfo, onError: " + errorMessage);
            }
        });
    }
}
