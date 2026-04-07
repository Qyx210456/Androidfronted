package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import java.util.List;

public class LoanOrdersViewModel extends AndroidViewModel {
    private static final String TAG = "LoanOrdersViewModel";
    private final LoanOrderRepository repository;
    
    private final MutableLiveData<List<LoanOrderEntity>> loanOrders = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    
    public LoanOrdersViewModel(Application application) {
        super(application);
        this.repository = LoanOrderRepository.getInstance(application);
    }
    
    public MutableLiveData<List<LoanOrderEntity>> getLoanOrders() {
        return loanOrders;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public void loadLoanOrders() {
        isLoading.setValue(true);
        repository.getLoanOrders(new LoanOrderRepository.LoanOrdersCallback() {
            @Override
            public void onSuccess(List<LoanOrderEntity> orders) {
                Log.d(TAG, "Loaded " + orders.size() + " loan orders");
                loanOrders.setValue(orders);
                isLoading.setValue(false);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load loan orders: " + errorMessage);
                LoanOrdersViewModel.this.errorMessage.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }
    
    public void loadLoanOrdersFromLocal() {
        repository.getLoanOrdersFromLocal(new LoanOrderRepository.LoanOrdersCallback() {
            @Override
            public void onSuccess(List<LoanOrderEntity> orders) {
                Log.d(TAG, "Loaded " + orders.size() + " loan orders from local");
                loanOrders.setValue(orders);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load loan orders from local: " + errorMessage);
                LoanOrdersViewModel.this.errorMessage.setValue(errorMessage);
            }
        });
    }
    
    public void filterLoanOrdersByStatus(String status) {
        if ("全部".equals(status)) {
            loadLoanOrdersFromLocal();
        } else {
            repository.getLoanOrdersByStatusFromLocal(status, new LoanOrderRepository.LoanOrdersCallback() {
                @Override
                public void onSuccess(List<LoanOrderEntity> orders) {
                    Log.d(TAG, "Loaded " + orders.size() + " loan orders with status: " + status);
                    loanOrders.setValue(orders);
                }
                
                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Failed to load loan orders by status: " + errorMessage);
                    LoanOrdersViewModel.this.errorMessage.setValue(errorMessage);
                }
            });
        }
    }
}
