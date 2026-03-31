package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.local.entity.CertificationEntity;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class PersonalInfoViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<CertInfoResponse> certInfoResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasIdInfo = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> hasJobInfo = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> hasPropertyInfo = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> hasThirdPartyInfo = new MutableLiveData<>(null);

    public PersonalInfoViewModel(@NonNull Application application) {
        super(application);
        android.util.Log.d("PersonalInfoViewModel", "PersonalInfoViewModel constructor called");
        this.repository = AuthRepository.getInstance(application);
        // 在构造函数中加载本地认证信息
        loadLocalCertInfo();
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
        
        // 本地认证信息已经在构造函数中加载，这里直接请求网络获取最新数据
        // 不显示加载状态，避免页面闪烁
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                android.util.Log.d("PersonalInfoViewModel", "getCertInfo onSuccess called");
                certInfoResult.postValue(response);
                
                if (response != null && response.getData() != null) {
                    CertInfoResponse.CertInfoData data = response.getData();
                    CertInfoResponse.UserCert userCert = data.getUserCert();
                    
                    if (userCert != null) {
                        hasIdInfo.postValue(userCert.getIdCard() != null && !userCert.getIdCard().isEmpty());
                        hasJobInfo.postValue(userCert.getWorkCertId() > 0);
                        hasThirdPartyInfo.postValue(userCert.getTriCertId() > 0);
                        hasPropertyInfo.postValue(userCert.getImmovableCertId() > 0);
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                android.util.Log.d("PersonalInfoViewModel", "getCertInfo onError called: " + errorMsg);
                // 网络请求失败时，保持本地数据
            }
        });
    }
    
    private void loadLocalCertInfo() {
        android.util.Log.d("PersonalInfoViewModel", "loadLocalCertInfo called");
        repository.getLocalCertInfo(new AuthRepository.AuthCallback<CertificationEntity>() {
            @Override
            public void onSuccess(CertificationEntity certification) {
                if (certification != null) {
                    // 更新 LiveData，显示本地数据
                    hasIdInfo.postValue(certification.getIdCard() != null && !certification.getIdCard().isEmpty());
                    hasJobInfo.postValue(certification.getWorkCertId() > 0);
                    hasThirdPartyInfo.postValue(certification.getTriCertId() > 0);
                    hasPropertyInfo.postValue(certification.getImmovableCertId() > 0);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // 本地数据加载失败，保持默认状态
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
