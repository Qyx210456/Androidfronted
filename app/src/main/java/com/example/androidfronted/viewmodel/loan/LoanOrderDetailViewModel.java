package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;

public class LoanOrderDetailViewModel extends BaseViewModel {
    private final LoanOrderRepository loanOrderRepository;
    private final MutableLiveData<LoanOrderDetailEntity> orderDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoanOrderDetailViewModel(@NonNull Application application, LoanOrderRepository loanOrderRepository) {
        super(application);
        this.loanOrderRepository = loanOrderRepository;
    }

    public MutableLiveData<LoanOrderDetailEntity> getOrderDetail() {
        return orderDetail;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadOrderDetail(int orderId) {
        showLoading();
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                hideLoading();
                orderDetail.postValue(detail);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public String generateOrderNumber(LoanOrderDetailEntity detail) {
        String startTime = detail.getStartTime();
        String dateTimePart = startTime.replaceAll("[^0-9]", "").substring(0, 12);
        return "LN" + dateTimePart + detail.getId();
    }

    public String formatCurrentTerm(LoanOrderDetailEntity detail) {
        return "第" + detail.getCurrentTerm() + "/" + detail.getTerm() + "期";
    }

    public int getStatusBackground(String status) {
        switch (status) {
            case "正常":
                return R.drawable.bg_loan_order_corner_status_normal;
            case "已逾期":
                return R.drawable.bg_loan_order_corner_status_overdue;
            case "已完成":
                return R.drawable.bg_loan_order_corner_status_completed;
            default:
                return R.drawable.bg_loan_order_corner_status_normal;
        }
    }

    public int getStatusTextColor(String status) {
        switch (status) {
            case "正常":
                return R.color.order_status_normal_text;
            case "已逾期":
                return R.color.order_status_overdue_text;
            case "已完成":
                return R.color.order_status_completed_text;
            default:
                return R.color.order_status_normal_text;
        }
    }

    public String formatLoanPeriod(int loanPeriod) {
        return loanPeriod + "年";
    }

    public String formatInterestRate(double interestRate) {
        return String.format("%.2f%%", interestRate * 100);
    }

    protected void showLoading() {
        isLoading.postValue(true);
    }

    protected void hideLoading() {
        isLoading.postValue(false);
    }

    protected void showError(String message) {
        errorMessage.postValue(message);
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }

    public void repayLoanOrder(int orderId) {
        showLoading();
        loanOrderRepository.repayLoanOrder(orderId, new LoanOrderRepository.RepayCallback() {
            @Override
            public void onSuccess(String message) {
                hideLoading();
                errorMessage.postValue(message);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                errorMessage.postValue(errorMsg);
            }
        });
    }
}
