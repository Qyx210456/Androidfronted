package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import java.text.DecimalFormat;
import java.util.List;

public class PaymentViewModel extends BaseViewModel {
    private static final String TAG = "PaymentViewModel";
    
    private final LoanOrderRepository loanOrderRepository;
    
    private final MutableLiveData<LoanOrderDetailEntity> orderDetail = new MutableLiveData<>();
    private final MutableLiveData<RepaymentPlanEntity> currentTermPlan = new MutableLiveData<>();
    private final MutableLiveData<String> selectedPaymentMethod = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> paymentSuccess = new MutableLiveData<>(false);
    
    private int orderId;
    private int currentTerm;

    public PaymentViewModel(@NonNull Application application, LoanOrderRepository loanOrderRepository) {
        super(application);
        this.loanOrderRepository = loanOrderRepository;
    }

    public LiveData<LoanOrderDetailEntity> getOrderDetail() {
        return orderDetail;
    }

    public LiveData<RepaymentPlanEntity> getCurrentTermPlan() {
        return currentTermPlan;
    }

    public LiveData<String> getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }

    public LiveData<Boolean> getPaymentSuccess() {
        return paymentSuccess;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setSelectedPaymentMethod(String method) {
        selectedPaymentMethod.setValue(method);
    }

    public void loadPaymentData(int orderId) {
        this.orderId = orderId;
        showLoading();
        
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    orderDetail.postValue(detail);
                    currentTerm = detail.getCurrentTerm();
                    loadCurrentTermPlan(orderId, currentTerm);
                } else {
                    hideLoading();
                    showError("获取订单详情失败");
                }
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    private void loadCurrentTermPlan(int orderId, int currentTerm) {
        loanOrderRepository.getRepaymentPlan(orderId, currentTerm, new LoanOrderRepository.RepaymentPlanCallback() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                hideLoading();
                if (plans != null && !plans.isEmpty()) {
                    int targetTerm = currentTerm + 1;
                    for (RepaymentPlanEntity plan : plans) {
                        if (plan.getTerm() == targetTerm) {
                            currentTermPlan.postValue(plan);
                            return;
                        }
                    }
                    if (!plans.isEmpty()) {
                        currentTermPlan.postValue(plans.get(0));
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public void executePayment() {
        String method = selectedPaymentMethod.getValue();
        if (method == null || method.isEmpty()) {
            showError("请选择支付方式");
            return;
        }
        
        showLoading();
        Log.d(TAG, "executePayment for orderId: " + orderId);
        
        loanOrderRepository.repayLoanOrder(orderId, new LoanOrderRepository.RepayCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "repayLoanOrder onSuccess: " + message);
                updateLocalCurrentTerm();
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "repayLoanOrder onError: " + errorMsg);
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    private void updateLocalCurrentTerm() {
        loanOrderRepository.updateCurrentTerm(orderId, currentTerm + 1, new LoanOrderRepository.UpdateCurrentTermCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "updateCurrentTerm onSuccess");
                hideLoading();
                paymentSuccess.postValue(true);
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "updateCurrentTerm onError: " + errorMsg);
                hideLoading();
                paymentSuccess.postValue(true);
            }
        });
    }

    public String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "¥" + df.format(amount);
    }

    public String getTermText() {
        int term = currentTerm + 1;
        return "第" + term + "期还款";
    }

    public String getSuccessTermText() {
        int term = currentTerm + 1;
        return "您的第" + term + "期还款已提交成功";
    }
}
