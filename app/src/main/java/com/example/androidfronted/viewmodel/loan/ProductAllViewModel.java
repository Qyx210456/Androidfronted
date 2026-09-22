package com.example.androidfronted.viewmodel.loan;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.data.repository.LoanProductRepository;
import com.example.androidfronted.ui.adapter.LoanProductAdapter;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductAllViewModel extends BaseViewModel {
    /** 产品分类常量：与 product_all.xml 分类栏顺序一致 */
    public static final int CATEGORY_ALL = 0;
    public static final int CATEGORY_PERSONAL = 1;
    public static final int CATEGORY_AGRICULTURAL = 2;
    public static final int CATEGORY_BUSINESS = 3;

    private final LoanProductRepository repository;
    private final MutableLiveData<List<LoanProduct>> products = new MutableLiveData<>();
    private final MutableLiveData<String> currentSortField = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentCategory = new MutableLiveData<>(CATEGORY_ALL);
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

    public MutableLiveData<Integer> getCurrentCategory() {
        return currentCategory;
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

    /**
     * 切换产品分类（全部/个人消费/农业生产/企业经营），切换后重新执行筛选+排序
     */
    public void setCategory(int category) {
        Integer current = currentCategory.getValue();
        if (current != null && current == category) {
            return;
        }
        currentCategory.setValue(category);
        applySorting();
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
        // 先按分类筛选，再排序
        List<LoanProduct> filtered = new ArrayList<>();
        int category = currentCategory.getValue() != null ? currentCategory.getValue() : CATEGORY_ALL;
        for (LoanProduct product : originalProducts) {
            if (matchesCategory(product, category)) {
                filtered.add(product);
            }
        }

        String field = currentSortField.getValue();
        Boolean ascending = isAscending.getValue();

        if (field == null || ascending == null) {
            products.setValue(filtered);
            return;
        }

        switch (field) {
            case "rate":
                filtered.sort((p1, p2) -> {
                    double r1 = getMinRate(p1);
                    double r2 = getMinRate(p2);
                    return ascending ? Double.compare(r1, r2) : Double.compare(r2, r1);
                });
                break;
            case "amount":
                filtered.sort((p1, p2) -> {
                    double a1 = getMaxAmount(p1);
                    double a2 = getMaxAmount(p2);
                    return ascending ? Double.compare(a1, a2) : Double.compare(a2, a1);
                });
                break;
            case "term":
                filtered.sort((p1, p2) -> {
                    int t1 = getMinTerm(p1);
                    int t2 = getMinTerm(p2);
                    return ascending ? Integer.compare(t1, t2) : Integer.compare(t2, t1);
                });
                break;
        }
        products.setValue(filtered);
    }

    /**
     * 分类匹配规则与卡片样式兜底一致：null/未知类型按个人消费处理
     */
    private boolean matchesCategory(LoanProduct product, int category) {
        int type = LoanProductAdapter.resolveType(product.getProductType());
        switch (category) {
            case CATEGORY_PERSONAL:
                return type == 0;
            case CATEGORY_AGRICULTURAL:
                return type == 1;
            case CATEGORY_BUSINESS:
                return type == 2;
            case CATEGORY_ALL:
            default:
                return true;
        }
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
