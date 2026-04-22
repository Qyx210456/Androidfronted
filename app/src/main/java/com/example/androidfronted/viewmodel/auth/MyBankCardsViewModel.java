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

import java.util.ArrayList;
import java.util.List;

public class MyBankCardsViewModel extends BaseViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthSubmitResponse> submitResult = new MutableLiveData<>();
    private final MutableLiveData<CertState> certState = new MutableLiveData<>();
    private final MutableLiveData<List<BankCardItem>> bankCardData = new MutableLiveData<>(new ArrayList<>());
    private CertState previousState = null;

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
        // 在构造函数中加载本地认证信息
        loadLocalCertInfo();
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
        Log.d("MyBankCardsViewModel", "getCertInfo called");
        repository.getCertInfo(new AuthRepository.AuthCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                Log.d("MyBankCardsViewModel", "getCertInfo, onSuccess, response: " + (response != null ? "not null" : "null"));
                if (response != null && response.getData() != null) {
                    CertInfoResponse.CertInfoData data = response.getData();
                    CertInfoResponse.UserCert userCert = data.getUserCert();
                    Log.d("MyBankCardsViewModel", "getCertInfo, userCert: " + (userCert != null ? "not null" : "null"));
                    
                    if (userCert != null && userCert.getBankCardId() != null && !userCert.getBankCardId().isEmpty()) {
                        Log.d("MyBankCardsViewModel", "getCertInfo, bankCardId: " + userCert.getBankCardId());
                        certState.postValue(CertState.CERTIFIED);
                        List<BankCardItem> items = new ArrayList<>();
                        items.add(new BankCardItem("", userCert.getBankCardId()));
                        bankCardData.postValue(items);
                        Log.d("MyBankCardsViewModel", "getCertInfo, bankCardData set: " + items.size() + " items");
                    } else {
                        // 网络请求成功但数据为空时，保持当前状态，不设置为 NOT_CERTIFIED
                        // 这样可以避免覆盖本地的已认证状态，防止页面闪烁
                        Log.d("MyBankCardsViewModel", "getCertInfo, no bankCardId found, keeping current state");
                    }
                } else {
                    Log.d("MyBankCardsViewModel", "getCertInfo, response or data is null");
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.d("MyBankCardsViewModel", "getCertInfo, onError: " + errorMsg);
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

    public void submitBankCard(String bankCardNumber, String bankName) {
        if (previousState == null) {
            previousState = certState.getValue();
        }
        certState.setValue(CertState.UPLOADING);
        repository.submitOtherCert(bankCardNumber, null, null, null, null, null, null,
                new AuthRepository.AuthCallback<AuthSubmitResponse>() {
                    @Override
                    public void onSuccess(AuthSubmitResponse response) {
                        previousState = null;
                        submitResult.postValue(response);
                        getCertInfo();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        certState.postValue(previousState != null ? previousState : CertState.NOT_CERTIFIED);
                        previousState = null;
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
    
    private void loadLocalCertInfo() {
        Log.d("MyBankCardsViewModel", "loadLocalCertInfo called");
        repository.getLocalBankCardState(new AuthRepository.AuthCallback<CertState>() {
            @Override
            public void onSuccess(CertState certStateValue) {
                Log.d("MyBankCardsViewModel", "loadLocalCertInfo, onSuccess, certState: " + certStateValue);
                certState.postValue(certStateValue);
                
                if (certStateValue == CertState.CERTIFIED) {
                    repository.getLocalBankCardData(new AuthRepository.AuthCallback<String>() {
                        @Override
                        public void onSuccess(String bankCardId) {
                            if (bankCardId != null && !bankCardId.isEmpty()) {
                                List<BankCardItem> items = new ArrayList<>();
                                items.add(new BankCardItem("", bankCardId));
                                bankCardData.postValue(items);
                                Log.d("MyBankCardsViewModel", "loadLocalCertInfo, loaded local bankCardData: " + bankCardId);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d("MyBankCardsViewModel", "loadLocalCertInfo, getLocalBankCardData error: " + errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("MyBankCardsViewModel", "loadLocalCertInfo, onError: " + errorMessage);
            }
        });
    }
}
