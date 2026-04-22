package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import java.text.DecimalFormat;

public class LoanManageViewModel extends BaseViewModel {
    private final LoanOrderRepository loanOrderRepository;
    
    private final MutableLiveData<Double> totalPrincipal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalInterest = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);

    public LoanManageViewModel(@NonNull Application application, LoanOrderRepository loanOrderRepository) {
        super(application);
        this.loanOrderRepository = loanOrderRepository;
    }

    public LiveData<Double> getTotalPrincipal() {
        return totalPrincipal;
    }

    public LiveData<Double> getTotalInterest() {
        return totalInterest;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public void loadUnpaidStats() {
        loanOrderRepository.getAllUnpaidStats(new LoanOrderRepository.UnpaidStatsCallback() {
            @Override
            public void onSuccess(double principal, double interest, double amount) {
                totalPrincipal.postValue(principal);
                totalInterest.postValue(interest);
                totalAmount.postValue(amount);
            }

            @Override
            public void onError(String errorMessage) {
                totalPrincipal.postValue(0.0);
                totalInterest.postValue(0.0);
                totalAmount.postValue(0.0);
            }
        });
    }

    public String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }
}
