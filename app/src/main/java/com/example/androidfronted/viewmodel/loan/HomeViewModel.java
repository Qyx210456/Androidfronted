package com.example.androidfronted.viewmodel.loan;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.data.repository.LoanProductRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

import java.util.List;

public class HomeViewModel extends BaseViewModel {
    private final LoanProductRepository repository;
    private final MutableLiveData<List<LoanProduct>> loanProducts = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.repository = LoanProductRepository.getInstance(application);
    }

    public MutableLiveData<List<LoanProduct>> getLoanProducts() {
        return loanProducts;
    }

    public void loadLoanProducts() {
        showLoading();
        repository.getLoanProducts(new LoanProductRepository.AuthCallback<List<LoanProduct>>() {
            @Override
            public void onSuccess(List<LoanProduct> products) {
                hideLoading();
                loanProducts.postValue(products);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public void navigateToProductDetail(LoanProduct product) {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_PRODUCT_DETAIL, product);
    }
}
