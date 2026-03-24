package com.example.androidfronted.viewmodel.loan;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.ProductApplyRequest;
import com.example.androidfronted.data.repository.LoanApplicationRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class ProductApplyViewModel extends BaseViewModel {
    private final LoanApplicationRepository repository;
    private final MutableLiveData<String> submitResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>();

    public ProductApplyViewModel(@NonNull Application application) {
        super(application);
        this.repository = LoanApplicationRepository.getInstance(application);
    }

    public MutableLiveData<String> getSubmitResult() {
        return submitResult;
    }

    public MutableLiveData<Boolean> getSubmitSuccess() {
        return submitSuccess;
    }

    public void submitApplication(ProductApplyRequest request) {
        showLoading();
        repository.submitApplication(request, new LoanApplicationRepository.CallbackResult() {
            @Override
            public void onSuccess(String successMessage) {
                hideLoading();
                submitResult.postValue(successMessage);
                submitSuccess.postValue(true);
                navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_HOME);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
                submitSuccess.postValue(false);
            }
        });
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }
}
