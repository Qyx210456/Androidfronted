package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class PersonalInfoViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<CertInfoResponse> certInfoResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasIdInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasJobInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasPropertyInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasThirdPartyInfo = new MutableLiveData<>();

    public PersonalInfoViewModel(@NonNull Application application) {
        super(application);
        android.util.Log.d("PersonalInfoViewModel", "PersonalInfoViewModel constructor called");
        this.repository = AuthRepository.getInstance(application);
    }

    public MutableLiveData<CertInfoResponse> getCertInfoResult() {
        return certInfoResult;
    }

    public MutableLiveData<Boolean> getHasIdInfo() {
        return hasIdInfo;
    }

    public MutableLiveData<Boolean> getHasJobInfo() {
        return hasJobInfo;
    }

    public MutableLiveData<Boolean> getHasPropertyInfo() {
        return hasPropertyInfo;
    }

    public MutableLiveData<Boolean> getHasThirdPartyInfo() {
        return hasThirdPartyInfo;
    }

    public void getCertInfo() {
        android.util.Log.d("PersonalInfoViewModel", "getCertInfo called");
        showLoading();
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                android.util.Log.d("PersonalInfoViewModel", "getCertInfo onSuccess called");
                hideLoading();
                certInfoResult.postValue(response);
                
                if (response != null && response.getData() != null) {
                    CertInfoResponse.CertInfoData data = response.getData();
                    CertInfoResponse.UserCert userCert = data.getUserCert();
                    
                    if (userCert != null) {
                        hasIdInfo.postValue(userCert.getIdCard() != null && !userCert.getIdCard().isEmpty());
                        hasJobInfo.postValue(userCert.getWorkCertId() > 0);
                        hasThirdPartyInfo.postValue(userCert.getTriCertId() > 0);
                        hasPropertyInfo.postValue(userCert.getImmovableCertId() > 0);
                    } else {
                        hasIdInfo.postValue(false);
                        hasJobInfo.postValue(false);
                        hasPropertyInfo.postValue(false);
                        hasThirdPartyInfo.postValue(false);
                    }
                } else {
                    hasIdInfo.postValue(false);
                    hasJobInfo.postValue(false);
                    hasPropertyInfo.postValue(false);
                    hasThirdPartyInfo.postValue(false);
                }
            }

            @Override
            public void onError(String errorMsg) {
                android.util.Log.d("PersonalInfoViewModel", "getCertInfo onError called: " + errorMsg);
                hideLoading();
                showError(errorMsg);
                hasIdInfo.postValue(false);
                hasJobInfo.postValue(false);
                hasPropertyInfo.postValue(false);
                hasThirdPartyInfo.postValue(false);
            }
        });
    }

    public void navigateToIdCert() {
        navigate(NavigationEvent.NAVIGATE_TO_ID_CERT_UPLOAD);
    }

    public void navigateToJobCert() {
        navigate(NavigationEvent.NAVIGATE_TO_JOB_CERT_UPLOAD);
    }

    public void navigateToPropertyCert() {
        navigate(NavigationEvent.NAVIGATE_TO_PROPERTY_CERT_UPLOAD);
    }

    public void navigateToThirdPartyCert() {
        navigate(NavigationEvent.NAVIGATE_TO_THIRD_PARTY_CERT_UPLOAD);
    }

    public void navigateBack() {
        navigate(NavigationEvent.NAVIGATE_BACK);
    }
}
