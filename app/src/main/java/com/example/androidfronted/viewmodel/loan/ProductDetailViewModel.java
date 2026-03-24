package com.example.androidfronted.viewmodel.loan;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class ProductDetailViewModel extends BaseViewModel {
    private final MutableLiveData<LoanProduct> product = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedTerm = new MutableLiveData<>();
    private final MutableLiveData<Double> selectedAmount = new MutableLiveData<>();
    private final MutableLiveData<LoanProduct.LoanOption> selectedOption = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    public ProductDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<LoanProduct> getProduct() {
        return product;
    }

    public MutableLiveData<Integer> getSelectedTerm() {
        return selectedTerm;
    }

    public MutableLiveData<Double> getSelectedAmount() {
        return selectedAmount;
    }

    public MutableLiveData<LoanProduct.LoanOption> getSelectedOption() {
        return selectedOption;
    }

    public MutableLiveData<String> getValidationError() {
        return validationError;
    }

    public void setProduct(LoanProduct loanProduct) {
        product.setValue(loanProduct);
    }

    public void setSelectedTerm(int term) {
        selectedTerm.setValue(term);
    }

    public void setSelectedAmount(double amount) {
        selectedAmount.setValue(amount);
    }

    public void setSelectedOption(LoanProduct.LoanOption option) {
        selectedOption.setValue(option);
    }

    public void validateAndNavigateToApply() {
        LoanProduct currentProduct = product.getValue();
        Integer term = selectedTerm.getValue();
        Double amount = selectedAmount.getValue();
        LoanProduct.LoanOption option = selectedOption.getValue();

        if (currentProduct == null) {
            validationError.postValue("产品信息异常");
            return;
        }

        if (option == null) {
            validationError.postValue("请选择一个贷款方案");
            return;
        }

        if (term == null || term <= 0) {
            validationError.postValue("请选择还款期数");
            return;
        }

        if (amount == null || amount <= 0) {
            validationError.postValue("请输入贷款金额");
            return;
        }

        if (amount < currentProduct.getMinAmount()) {
            validationError.postValue("贷款金额不能小于" + currentProduct.getMinAmount() + "元");
            return;
        }

        if (amount > currentProduct.getMaxAmount()) {
            validationError.postValue("贷款金额不能大于" + currentProduct.getMaxAmount() + "元");
            return;
        }

        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_PRODUCT_APPLY);
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }
}
