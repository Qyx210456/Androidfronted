package com.example.androidfronted.viewmodel.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

import java.util.ArrayList;
import java.util.List;

public class MyBankCardsViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthSubmitResponse> submitResult = new MutableLiveData<>();
    private final MutableLiveData<CertState> certState = new MutableLiveData<>(CertState.NOT_CERTIFIED);
    private final MutableLiveData<List<BankCardItem>> bankCardData = new MutableLiveData<>(new ArrayList<>());
    private CertState previousState = CertState.NOT_CERTIFIED;

    public static class BankCardItem {
        private String bankName;
        private String cardNumber;

        public BankCardItem(String bankName, String cardNumber) {
            this.bankName = bankName;
            this.cardNumber = cardNumber;
        }

        public String getBankName() {
            return bankName;
        }

        public String getCardNumber() {
            return cardNumber;
        }
    }

    public MyBankCardsViewModel(@NonNull Application application) {
        super(application);
        this.repository = AuthRepository.getInstance(application);
    }

    public MutableLiveData<AuthSubmitResponse> getSubmitResult() {
        return submitResult;
    }

    public MutableLiveData<CertState> getCertState() {
        return certState;
    }

    public MutableLiveData<List<BankCardItem>> getBankCardData() {
        return bankCardData;
    }

    public void getCertInfo() {
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                if (response != null && response.getData() != null) {
                    CertInfoResponse.CertInfoData data = response.getData();
                    CertInfoResponse.UserCert userCert = data.getUserCert();
                    
                    if (userCert != null && userCert.getBankCardId() != null && !userCert.getBankCardId().isEmpty()) {
                        certState.postValue(CertState.CERTIFIED);
                        List<BankCardItem> items = new ArrayList<>();
                        items.add(new BankCardItem("", userCert.getBankCardId()));
                        bankCardData.postValue(items);
                    } else {
                        certState.postValue(CertState.NOT_CERTIFIED);
                        bankCardData.postValue(new ArrayList<>());
                    }
                } else {
                    certState.postValue(CertState.NOT_CERTIFIED);
                    bankCardData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onError(String errorMsg) {
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

    public void submitBankCard(String bankCardNumber, String bankName) {
        certState.setValue(CertState.UPLOADING);
        repository.submitOtherCert(bankCardNumber, null, null, null, null, null, null,
                new AuthRepository.AuthCallback<AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(AuthSubmitResponse response) {
                        submitResult.postValue(response);
                        getCertInfo();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        certState.postValue(CertState.NOT_CERTIFIED);
                        showError(errorMsg);
                    }
                });
    }

    public void navigateToUpload() {
        navigate(NavigationEvent.NAVIGATE_TO_BANK_CARD_UPLOAD);
    }

    public void navigateBack() {
        navigate(NavigationEvent.NAVIGATE_BACK);
    }
}
