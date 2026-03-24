package com.example.androidfronted.viewmodel.loan;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.data.repository.LoanProductRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductAllViewModel extends BaseViewModel {
    private final LoanProductRepository repository;
    private final MutableLiveData<List<LoanProduct>> products = new MutableLiveData<>();
    private final MutableLiveData<String> currentSortField = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>();
    private final MutableLiveData<LoanProduct> selectedProduct = new MutableLiveData<>();

    private List<LoanProduct> originalProducts = new ArrayList<>();

    public ProductAllViewModel(@NonNull Application application) {
        super(application);
        this.repository = LoanProductRepository.getInstance(application);
        currentSortField.setValue("default");
        isAscending.setValue(true);
    }

    public MutableLiveData<List<LoanProduct>> getProducts() {
        return products;
    }

    public MutableLiveData<String> getCurrentSortField() {
        return currentSortField;
    }

    public MutableLiveData<Boolean> getIsAscending() {
        return isAscending;
    }

    public MutableLiveData<LoanProduct> getSelectedProduct() {
        return selectedProduct;
    }

    public void loadProducts() {
        showLoading();
        repository.getLoanProducts(new LoanProductRepository.AuthCallback<List<LoanProduct>>() {
            @Override
            public void onSuccess(List<LoanProduct> productList) {
                hideLoading();
                if (productList != null && !productList.isEmpty()) {
                    originalProducts = new ArrayList<>(productList);
                    applySorting();
                }
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                showError(errorMessage);
            }
        });
    }

    public void sortProducts(String field) {
        String currentField = currentSortField.getValue();
        Boolean ascending = isAscending.getValue();

        if (field.equals(currentField)) {
            isAscending.setValue(!(ascending != null && ascending));
        } else {
            currentSortField.setValue(field);
            isAscending.setValue(true);
        }
        applySorting();
    }

    private void applySorting() {
        List<LoanProduct> sorted = new ArrayList<>(originalProducts);
        String field = currentSortField.getValue();
        Boolean ascending = isAscending.getValue();

        if (field == null || ascending == null) {
            products.setValue(sorted);
            return;
        }

        switch (field) {
            case "rate":
                sorted.sort((p1, p2) -> {
                    double r1 = getMinRate(p1);
                    double r2 = getMinRate(p2);
                    return ascending ? Double.compare(r1, r2) : Double.compare(r2, r1);
                });
                break;
            case "amount":
                sorted.sort((p1, p2) -> {
                    double a1 = getMaxAmount(p1);
                    double a2 = getMaxAmount(p2);
                    return ascending ? Double.compare(a1, a2) : Double.compare(a2, a1);
                });
                break;
            case "term":
                sorted.sort((p1, p2) -> {
                    int t1 = getMinTerm(p1);
                    int t2 = getMinTerm(p2);
                    return ascending ? Integer.compare(t1, t2) : Integer.compare(t2, t1);
                });
                break;
        }
        products.setValue(sorted);
    }

    private double getMinRate(LoanProduct p) {
        if (p.getOptions() == null || p.getOptions().isEmpty()) return Double.MAX_VALUE;
        return p.getOptions().stream()
                .mapToDouble(LoanProduct.LoanOption::getInterestRate)
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private double getMaxAmount(LoanProduct p) {
        return p.getMaxAmount();
    }

    private int getMinTerm(LoanProduct p) {
        List<Integer> terms = p.getTerms();
        if (terms == null || terms.isEmpty()) return Integer.MAX_VALUE;
        return Collections.min(terms);
    }

    public void selectProduct(LoanProduct product) {
        selectedProduct.setValue(product);
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_TO_PRODUCT_DETAIL, product);
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }
}