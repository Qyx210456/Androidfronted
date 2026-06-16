package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import android.app.Application;
import android.util.Log;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.AuthRepository;

public class JobCertViewModel extends BaseCertViewModel<CertInfoResponse.WorkCert> {

    public JobCertViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void getCertInfo(CertInfoCallback callback) {
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                if (response != null && response.getData() != null) {
                    CertInfoResponse.WorkCert workCert = response.getData().getWorkCert();
                    CertInfoResponse.UserCert userCert = response.getData().getUserCert();
                    
                    certData.postValue(workCert);
                    
                    if (userCert != null && userCert.getBankCardId() != null) {
                        bankCardId.postValue(userCert.getBankCardId());
                        Log.d("JobCertViewModel", "getCertInfo, bankCardId: " + userCert.getBankCardId());
                    }
                    
                    if (workCert != null && 
                        (workCert.getEmploymentCertPath() != null && !workCert.getEmploymentCertPath().isEmpty() ||
                         workCert.getSalaryCertPath() != null && !workCert.getSalaryCertPath().isEmpty())) {
                        CertState currentState = certState.getValue();
                        if (currentState != CertState.UPLOADING && currentState != null) {
                            certState.postValue(CertState.CERTIFIED);
                        }
                    } else {
                        Log.d("JobCertViewModel", "getCertInfo, workCert is null or empty, keeping current state");
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
    public void submitCert(okhttp3.RequestBody employmentFile, okhttp3.RequestBody salaryFile) {
        Log.d("JobCertViewModel", "submitCert called");
        Log.d("JobCertViewModel", "submitCert, employmentFile: " + (employmentFile != null ? "present" : "null"));
        Log.d("JobCertViewModel", "submitCert, salaryFile: " + (salaryFile != null ? "present" : "null"));
        if (previousState == null) {
            previousState = certState.getValue();
        }
        certState.setValue(CertState.UPLOADING);
        Log.d("JobCertViewModel", "submitCert, state changed to UPLOADING");
        repository.submitOtherCert("", null, null, employmentFile, salaryFile, null, null,
                new AuthRepository.AuthCallback<com.example.androidfronted.data.model.AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(com.example.androidfronted.data.model.AuthSubmitResponse response) {
                        Log.d("JobCertViewModel", "submitCert, onSuccess");
                        handleSuccess(response);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e("JobCertViewModel", "submitCert, onError: " + errorMsg);
                        handleError(errorMsg);
                    }
                });
    }

    public void navigateToUpload() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_JOB_CERT_UPLOAD);
    }

    @Override
    protected void loadLocalCertInfo() {
        Log.d("JobCertViewModel", "loadLocalCertInfo called");
        repository.getLocalJobCertState(new AuthRepository.AuthCallback<CertState>() {
            @Override
            public void onSuccess(CertState certStateValue) {
                Log.d("JobCertViewModel", "loadLocalCertInfo, onSuccess, certState: " + certStateValue);
                certState.postValue(certStateValue);
                
                if (certStateValue == CertState.CERTIFIED) {
                    repository.getLocalJobCertData(new AuthRepository.AuthCallback<CertInfoResponse.WorkCert>() {
                        @Override
                        public void onSuccess(CertInfoResponse.WorkCert workCert) {
                            if (workCert != null) {
                                certData.postValue(workCert);
                                Log.d("JobCertViewModel", "loadLocalCertInfo, loaded local workCert");
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d("JobCertViewModel", "loadLocalCertInfo, getLocalJobCertData error: " + errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("JobCertViewModel", "loadLocalCertInfo, onError: " + errorMessage);
            }
        });
    }
}
