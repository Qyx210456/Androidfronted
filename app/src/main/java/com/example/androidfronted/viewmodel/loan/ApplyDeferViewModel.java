package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import java.text.DecimalFormat;
import java.util.List;

public class ApplyDeferViewModel extends BaseViewModel {
    private static final String TAG = "ApplyDeferViewModel";
    private static final int DEFER_DAYS = 30;
    
    private final LoanOrderRepository loanOrderRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> submitSuccess = new MutableLiveData<>();
    
    private final MutableLiveData<Double> currentTotalAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> currentPrincipal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> currentInterest = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> dueDate = new MutableLiveData<>("");
    private final MutableLiveData<String> currentTerm = new MutableLiveData<>("");
    
    private final MutableLiveData<Integer> remainingTerms = new MutableLiveData<>(0);
    private final MutableLiveData<Double> remainingTotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> remainingPrincipal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> remainingInterest = new MutableLiveData<>(0.0);
    
    private final MutableLiveData<Double> deferFee = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> extraInterest = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> nextPayment = new MutableLiveData<>(0.0);
    
    private double annualRate = 0.0;

    public ApplyDeferViewModel(@NonNull Application application, LoanOrderRepository loanOrderRepository) {
        super(application);
        this.loanOrderRepository = loanOrderRepository;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<String> getSubmitSuccess() {
        return submitSuccess;
    }

    public MutableLiveData<Double> getCurrentTotalAmount() {
        return currentTotalAmount;
    }

    public MutableLiveData<Double> getCurrentPrincipal() {
        return currentPrincipal;
    }

    public MutableLiveData<Double> getCurrentInterest() {
        return currentInterest;
    }

    public MutableLiveData<String> getDueDate() {
        return dueDate;
    }

    public MutableLiveData<String> getCurrentTerm() {
        return currentTerm;
    }

    public MutableLiveData<Integer> getRemainingTerms() {
        return remainingTerms;
    }

    public MutableLiveData<Double> getRemainingTotal() {
        return remainingTotal;
    }

    public MutableLiveData<Double> getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public MutableLiveData<Double> getRemainingInterest() {
        return remainingInterest;
    }

    public MutableLiveData<Double> getDeferFee() {
        return deferFee;
    }

    public MutableLiveData<Double> getExtraInterest() {
        return extraInterest;
    }

    public MutableLiveData<Double> getNextPayment() {
        return nextPayment;
    }

    public void loadData(int orderId) {
        showLoading();
        loadOrderDetail(orderId);
    }

    private void loadOrderDetail(int orderId) {
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                Log.d(TAG, "loadOrderDetail onSuccess: detail=" + (detail != null ? "not null" : "null"));
                if (detail != null) {
                    annualRate = detail.getInterestRate();
                    Log.d(TAG, "loadOrderDetail: annualRate=" + annualRate + ", currentTerm=" + detail.getCurrentTerm());
                    loadRepaymentPlan(orderId, detail.getCurrentTerm());
                } else {
                    hideLoading();
                    showError("获取订单详情失败");
                }
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
                Log.e(TAG, "loadOrderDetail onError: " + errorMsg);
            }
        });
    }

    private void loadRepaymentPlan(int orderId, int currentTermValue) {
        Log.d(TAG, "loadRepaymentPlan: currentTermValue=" + currentTermValue);
        
        loanOrderRepository.getRepaymentPlan(orderId, currentTermValue, new LoanOrderRepository.RepaymentPlanCallback() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                hideLoading();
                Log.d(TAG, "loadRepaymentPlan onSuccess: plans size=" + (plans != null ? plans.size() : 0));
                if (plans != null && !plans.isEmpty()) {
                    processRepaymentPlans(plans, currentTermValue);
                } else {
                    Log.e(TAG, "loadRepaymentPlan: plans is null or empty");
                }
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
                Log.e(TAG, "loadRepaymentPlan onError: " + errorMsg);
            }
        });
    }

    private void processRepaymentPlans(List<RepaymentPlanEntity> plans, int currentTermValue) {
        Log.d(TAG, "processRepaymentPlans: plans size=" + plans.size() + ", currentTermValue=" + currentTermValue);
        
        int actualCurrentTerm = currentTermValue + 1;
        Log.d(TAG, "processRepaymentPlans: actualCurrentTerm=" + actualCurrentTerm);
        
        RepaymentPlanEntity currentPlan = null;
        RepaymentPlanEntity nextPlan = null;
        int unpaidCount = 0;
        double totalRemaining = 0;
        double totalRemainingPrincipal = 0;
        double totalRemainingInterest = 0;
        int minUnpaidTerm = Integer.MAX_VALUE;

        for (RepaymentPlanEntity plan : plans) {
            Log.d(TAG, "processRepaymentPlans: plan term=" + plan.getTerm() + ", status=" + plan.getStatus() + ", totalAmount=" + plan.getTotalAmount());
            
            if ("未还".equals(plan.getStatus())) {
                unpaidCount++;
                totalRemaining += plan.getTotalAmount();
                totalRemainingPrincipal += plan.getPrincipal();
                totalRemainingInterest += plan.getInterest();

                if (plan.getTerm() < minUnpaidTerm) {
                    minUnpaidTerm = plan.getTerm();
                }

                if (plan.getTerm() == actualCurrentTerm) {
                    currentPlan = plan;
                    Log.d(TAG, "processRepaymentPlans: found currentPlan by currentTermValue, term=" + plan.getTerm());
                } else if (plan.getTerm() == actualCurrentTerm + 1) {
                    nextPlan = plan;
                    Log.d(TAG, "processRepaymentPlans: found nextPlan by currentTermValue, term=" + plan.getTerm());
                }
            }
        }

        if (currentPlan == null && minUnpaidTerm != Integer.MAX_VALUE) {
            Log.d(TAG, "processRepaymentPlans: currentPlan not found by currentTermValue, using minUnpaidTerm=" + minUnpaidTerm);
            for (RepaymentPlanEntity plan : plans) {
                if ("未还".equals(plan.getStatus()) && plan.getTerm() == minUnpaidTerm) {
                    currentPlan = plan;
                    Log.d(TAG, "processRepaymentPlans: found currentPlan by minUnpaidTerm, term=" + plan.getTerm());
                    break;
                }
            }
            if (currentPlan != null) {
                for (RepaymentPlanEntity plan : plans) {
                    if ("未还".equals(plan.getStatus()) && plan.getTerm() == minUnpaidTerm + 1) {
                        nextPlan = plan;
                        Log.d(TAG, "processRepaymentPlans: found nextPlan by minUnpaidTerm, term=" + plan.getTerm());
                        break;
                    }
                }
            }
        }

        Log.d(TAG, "processRepaymentPlans: currentPlan=" + (currentPlan != null ? "found" : "null") + ", nextPlan=" + (nextPlan != null ? "found" : "null"));

        if (currentPlan != null) {
            Log.d(TAG, "processRepaymentPlans: setting currentPlan data - totalAmount=" + currentPlan.getTotalAmount() + ", principal=" + currentPlan.getPrincipal() + ", interest=" + currentPlan.getInterest() + ", dueDate=" + currentPlan.getDueDate());
            currentTotalAmount.postValue(currentPlan.getTotalAmount());
            currentPrincipal.postValue(currentPlan.getPrincipal());
            currentInterest.postValue(currentPlan.getInterest());
            dueDate.postValue(currentPlan.getDueDate());
            currentTerm.postValue("第 " + currentPlan.getTerm() + " 期");
            
            calculateExtraInterest(currentPlan.getPrincipal());
        } else {
            Log.e(TAG, "processRepaymentPlans: currentPlan is null!");
        }

        if (nextPlan != null) {
            nextPayment.postValue(nextPlan.getTotalAmount());
        } else {
            nextPayment.postValue(0.0);
        }

        remainingTerms.postValue(unpaidCount);
        remainingTotal.postValue(totalRemaining);
        remainingPrincipal.postValue(totalRemainingPrincipal);
        remainingInterest.postValue(totalRemainingInterest);
    }

    private void calculateExtraInterest(double principal) {
        extraInterest.postValue(0.0);
    }

    public void submitPostpone(int orderId) {
        showLoading();
        loanOrderRepository.applyPostpone(orderId, new LoanOrderRepository.PostponeCallback() {
            @Override
            public void onSuccess(String message) {
                hideLoading();
                submitSuccess.postValue(message);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "¥ " + df.format(amount);
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
}
