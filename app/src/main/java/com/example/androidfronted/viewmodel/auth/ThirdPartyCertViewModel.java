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
public class ThirdPartyCertViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthSubmitResponse> submitResult = new MutableLiveData<>();
    private final MutableLiveData<CertState> certState = new MutableLiveData<>();
    private final MutableLiveData<CertInfoResponse.TriCert> certData = new MutableLiveData<>();
    private final MutableLiveData<String> bankCardId = new MutableLiveData<>();
    private CertState previousState = null;

    public ThirdPartyCertViewModel(@NonNull Application application) {
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

    public MutableLiveData<CertInfoResponse.TriCert> getCertData() {
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
                    CertInfoResponse.TriCert triCert = response.getData().getTriCert();
                    CertInfoResponse.UserCert userCert = response.getData().getUserCert();
                    
                    certData.postValue(triCert);
                    
                    if (userCert != null && userCert.getBankCardId() != null) {
                        bankCardId.postValue(userCert.getBankCardId());
                    }
                    
                    if (triCert != null && 
                        (triCert.getSocialSecurityPath() != null && !triCert.getSocialSecurityPath().isEmpty() ||
                         triCert.getCreditReportPath() != null && !triCert.getCreditReportPath().isEmpty())) {
                        certState.postValue(CertState.CERTIFIED);
                    } else {
                        // 网络请求成功但数据为空时，保持当前状态，不设置为 NOT_CERTIFIED
                        // 这样可以避免覆盖本地的已认证状态，防止页面闪烁
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

    public void submitCert(okhttp3.RequestBody socialSecurityFile, okhttp3.RequestBody creditReportFile) {
        if (previousState == null) {
            previousState = certState.getValue();
        }
        certState.setValue(CertState.UPLOADING);
        repository.submitOtherCert("", null, null, null, null, socialSecurityFile, creditReportFile,
                new AuthRepository.AuthCallback<AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(AuthSubmitResponse response) {
                        previousState = null;
                        submitResult.postValue(response);
                        getCertInfo(null);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        certState.postValue(previousState != null ? previousState : CertState.NOT_CERTIFIED);
                        previousState = null;
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
        previousState = null;
    }

    public void navigateToUpload() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_THIRD_PARTY_CERT_UPLOAD);
    }
    
    private void loadLocalCertInfo() {
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
