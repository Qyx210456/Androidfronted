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

public class JobCertViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthSubmitResponse> submitResult = new MutableLiveData<>();
    private final MutableLiveData<CertState> certState = new MutableLiveData<>(CertState.NOT_CERTIFIED);
    private final MutableLiveData<CertInfoResponse.WorkCert> certData = new MutableLiveData<>();
    private final MutableLiveData<String> bankCardId = new MutableLiveData<>();
    private CertState previousState = CertState.NOT_CERTIFIED;

    public JobCertViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
    }

    public MutableLiveData<AuthSubmitResponse> getSubmitResult() {
        return submitResult;
    }

    public MutableLiveData<CertState> getCertState() {
        return certState;
    }

    public MutableLiveData<CertInfoResponse.WorkCert> getCertData() {
        return certData;
    }

    public MutableLiveData<String> getBankCardId() {
        return bankCardId;
    }

    public interface CertInfoCallback {
        void onSuccess(CertInfoResponse response);
        void onError(String errorMessage);
    }

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
                        certState.postValue(CertState.CERTIFIED);
                    } else {
                        certState.postValue(CertState.NOT_CERTIFIED);
                    }
                } else {
                    certState.postValue(CertState.NOT_CERTIFIED);
                }
                
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(String errorMsg) {
                certState.postValue(CertState.NOT_CERTIFIED);
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            }
        });
    }

    public void submitCert(okhttp3.RequestBody employmentFile, okhttp3.RequestBody salaryFile) {
        Log.d("JobCertViewModel", "submitCert called");
        Log.d("JobCertViewModel", "submitCert, employmentFile: " + (employmentFile != null ? "present" : "null"));
        Log.d("JobCertViewModel", "submitCert, salaryFile: " + (salaryFile != null ? "present" : "null"));
        certState.setValue(CertState.UPLOADING);
        Log.d("JobCertViewModel", "submitCert, state changed to UPLOADING");
        repository.submitOtherCert("", null, null, employmentFile, salaryFile, null, null,
                new AuthRepository.AuthCallback<AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(AuthSubmitResponse response) {
                        Log.d("JobCertViewModel", "submitCert, onSuccess");
                        submitResult.postValue(response);
                        getCertInfo(null);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e("JobCertViewModel", "submitCert, onError: " + errorMsg);
                        certState.postValue(CertState.NOT_CERTIFIED);
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
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_JOB_CERT_UPLOAD);
    }
}
