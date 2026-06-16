package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public abstract class BaseCertViewModel<T> extends BaseViewModel {
    protected final AuthRepository repository;
    protected final MutableLiveData<AuthSubmitResponse> submitResult = new MutableLiveData<>();
    protected final MutableLiveData<CertState> certState = new MutableLiveData<>();
    protected final MutableLiveData<T> certData = new MutableLiveData<>();
    protected final MutableLiveData<String> bankCardId = new MutableLiveData<>();
    protected CertState previousState = null;

    public BaseCertViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
        loadLocalCertInfo();
    }

    public MutableLiveData<AuthSubmitResponse> getSubmitResult() {
        return submitResult;
    }

    public MutableLiveData<CertState> getCertState() {
        return certState;
    }

    public MutableLiveData<T> getCertData() {
        return certData;
    }

    public MutableLiveData<String> getBankCardId() {
        return bankCardId;
    }

    public interface CertInfoCallback {
        void onSuccess(CertInfoResponse response);
        void onError(String errorMessage);
    }

    public abstract void getCertInfo(CertInfoCallback callback);

    public abstract void submitCert(okhttp3.RequestBody firstFile, okhttp3.RequestBody secondFile);

    public void startUpload() {
        previousState = certState.getValue();
        certState.setValue(CertState.UPLOADING);
    }

    public void cancelUpload() {
        certState.postValue(previousState);
        previousState = null;
    }

    protected abstract void loadLocalCertInfo();

    protected void handleSuccess(AuthSubmitResponse response) {
        previousState = null;
        submitResult.postValue(response);
        getCertInfo(null);
    }

    protected void handleError(String errorMsg) {
        certState.postValue(previousState != null ? previousState : CertState.NOT_CERTIFIED);
        previousState = null;
        showError(errorMsg);
    }
}
