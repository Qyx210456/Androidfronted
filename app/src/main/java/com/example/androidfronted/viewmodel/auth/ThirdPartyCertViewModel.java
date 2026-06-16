package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import android.app.Application;
import android.util.Log;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.AuthRepository;

public class ThirdPartyCertViewModel extends BaseCertViewModel<CertInfoResponse.TriCert> {

    public ThirdPartyCertViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void getCertInfo(CertInfoCallback callback) {
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                if (response != null && response.getData() != null) {
                    CertInfoResponse.TriCert triCert = response.getData().getTriCert();
                    CertInfoResponse.UserCert userCert = response.getData().getUserCert();
                    
                    certData.postValue(triCert);
                    
                    if (userCert != null && userCert.getBankCardId() != null) {
                        bankCardId.postValue(userCert.getBankCardId());
                    }
                    
                    if (triCert != null && 
                        (triCert.getSocialSecurityPath() != null && !triCert.getSocialSecurityPath().isEmpty() ||
                         triCert.getCreditReportPath() != null && !triCert.getCreditReportPath().isEmpty())) {
                        CertState currentState = certState.getValue();
                        if (currentState != CertState.UPLOADING && currentState != null) {
                            certState.postValue(CertState.CERTIFIED);
                        }
                    } else {
                        Log.d("ThirdPartyCertViewModel", "getCertInfo, triCert is null or empty, keeping current state");
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
    public void submitCert(okhttp3.RequestBody socialSecurityFile, okhttp3.RequestBody creditReportFile) {
        if (previousState == null) {
            previousState = certState.getValue();
        }
        certState.setValue(CertState.UPLOADING);
        repository.submitOtherCert("", null, null, null, null, socialSecurityFile, creditReportFile,
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
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_THIRD_PARTY_CERT_UPLOAD);
    }

    @Override
    protected void loadLocalCertInfo() {
        Log.d("ThirdPartyCertViewModel", "loadLocalCertInfo called");
        repository.getLocalThirdPartyCertState(new AuthRepository.AuthCallback<CertState>() {
            @Override
            public void onSuccess(CertState certStateValue) {
                Log.d("ThirdPartyCertViewModel", "loadLocalCertInfo, onSuccess, certState: " + certStateValue);
                certState.postValue(certStateValue);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("ThirdPartyCertViewModel", "loadLocalCertInfo, onError: " + errorMessage);
            }
        });
    }
}
