package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import java.text.DecimalFormat;
import java.util.List;

public class LoanOrderDetailViewModel extends BaseViewModel {
    private static final String TAG = "LoanOrderDetailVM";
    private final LoanOrderRepository loanOrderRepository;
    private final MutableLiveData<LoanOrderDetailEntity> orderDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> repaymentSuccess = new MutableLiveData<>();
    private final MutableLiveData<Double> totalLoanAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<Integer> progressPercent = new MutableLiveData<>(0);
    private final MutableLiveData<String> progressText = new MutableLiveData<>("0.00%");
    private final MutableLiveData<Double> unpaidTotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> unpaidPrincipal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> unpaidInterest = new MutableLiveData<>(0.0);

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

    public MutableLiveData<String> getRepaymentSuccess() {
        return repaymentSuccess;
    }

    public MutableLiveData<Double> getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public MutableLiveData<Integer> getProgressPercent() {
        return progressPercent;
    }

    public MutableLiveData<String> getProgressText() {
        return progressText;
    }

    public MutableLiveData<Double> getUnpaidTotal() {
        return unpaidTotal;
    }

    public MutableLiveData<Double> getUnpaidPrincipal() {
        return unpaidPrincipal;
    }

    public MutableLiveData<Double> getUnpaidInterest() {
        return unpaidInterest;
    }

    public void loadOrderDetail(int orderId) {
        showLoading();
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                hideLoading();
                orderDetail.postValue(detail);
                loadRepaymentPlanData(orderId, detail.getRepaidAmount(), detail.getCurrentTerm());
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public void refreshOrderDetail(int orderId) {
        final int[] localCurrentTerm = {-1};
        
        loanOrderRepository.getLoanOrderDetailFromLocal(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    localCurrentTerm[0] = detail.getCurrentTerm();
                    Log.d(TAG, "refreshOrderDetail: loaded from local, currentTerm=" + localCurrentTerm[0]);
                    orderDetail.postValue(detail);
                    loadRepaymentPlanData(orderId, detail.getRepaidAmount(), detail.getCurrentTerm());
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "refreshOrderDetail: failed to load from local: " + errorMsg);
            }
        });
        
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    int serverCurrentTerm = detail.getCurrentTerm();
                    Log.d(TAG, "refreshOrderDetail: loaded from server, serverCurrentTerm=" + serverCurrentTerm 
                            + ", localCurrentTerm=" + localCurrentTerm[0]);
                    
                    if (serverCurrentTerm > localCurrentTerm[0]) {
                        Log.d(TAG, "refreshOrderDetail: server currentTerm is newer, updating UI");
                        orderDetail.postValue(detail);
                        loadRepaymentPlanData(orderId, detail.getRepaidAmount(), detail.getCurrentTerm());
                    } else {
                        Log.d(TAG, "refreshOrderDetail: server currentTerm is not newer, skip UI update");
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "refreshOrderDetail: failed to load from server: " + errorMsg);
            }
        });
    }

    public void loadOrderDetailFromLocal(int orderId) {
        loanOrderRepository.getLoanOrderDetailFromLocal(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    orderDetail.postValue(detail);
                    loadRepaymentPlanData(orderId, detail.getRepaidAmount(), detail.getCurrentTerm());
                }
            }

            @Override
            public void onError(String errorMsg) {
            }
        });
    }

    private void loadRepaymentPlanData(int orderId, double repaidAmount, int currentTerm) {
        loanOrderRepository.getRepaymentPlan(orderId, currentTerm, new LoanOrderRepository.RepaymentPlanCallback() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                if (plans != null && !plans.isEmpty()) {
                    double total = 0;
                    double unpaidTotalSum = 0;
                    double unpaidPrincipalSum = 0;
                    double unpaidInterestSum = 0;
                    
                    for (RepaymentPlanEntity plan : plans) {
                        total += plan.getTotal();
                        
                        if ("未还".equals(plan.getStatus())) {
                            unpaidTotalSum += plan.getTotal();
                            unpaidPrincipalSum += plan.getPrincipal();
                            unpaidInterestSum += plan.getInterest();
                        }
                    }
                    
                    totalLoanAmount.postValue(total);
                    unpaidTotal.postValue(unpaidTotalSum);
                    unpaidPrincipal.postValue(unpaidPrincipalSum);
                    unpaidInterest.postValue(unpaidInterestSum);
                    calculateProgress(repaidAmount, total);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load repayment plan: " + errorMessage);
            }
        });
    }

    private void calculateProgress(double repaidAmount, double totalAmount) {
        if (totalAmount <= 0) {
            progressPercent.postValue(0);
            progressText.postValue("0.00%");
            return;
        }
        
        double percentage = (repaidAmount / totalAmount) * 100;
        int progress = (int) percentage;
        if (progress > 100) progress = 100;
        
        progressPercent.postValue(progress);
        
        DecimalFormat df = new DecimalFormat("0.00");
        progressText.postValue(df.format(percentage) + "%");
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

    public String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "¥" + df.format(amount);
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
        Log.d(TAG, "repayLoanOrder called for orderId: " + orderId);
        showLoading();
        
        final int[] previousCurrentTerm = {0};
        LoanOrderDetailEntity currentDetail = orderDetail.getValue();
        if (currentDetail != null) {
            previousCurrentTerm[0] = currentDetail.getCurrentTerm();
            Log.d(TAG, "Previous currentTerm: " + previousCurrentTerm[0]);
        }
        
        loanOrderRepository.repayLoanOrder(orderId, new LoanOrderRepository.RepayCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "repayLoanOrder onSuccess: " + message);
                loanOrderRepository.updateFirstUnpaidToRepaid(orderId, new LoanOrderRepository.UpdateFirstUnpaidCallback() {
                    @Override
                    public void onSuccess(int updatedTerm) {
                        Log.d(TAG, "updateFirstUnpaidToRepaid onSuccess, updatedTerm: " + updatedTerm);
                        hideLoading();
                        int termToShow = previousCurrentTerm[0] + 1;
                        repaymentSuccess.postValue("第" + termToShow + "期还款成功");
                        loadOrderDetail(orderId);
                    }

                    @Override
                    public void onAllRepaid() {
                        Log.d(TAG, "updateFirstUnpaidToRepaid onAllRepaid");
                        hideLoading();
                        repaymentSuccess.postValue("所有期数已还清，订单已完成");
                        loadOrderDetail(orderId);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e(TAG, "updateFirstUnpaidToRepaid onError: " + errorMsg);
                        hideLoading();
                        int termToShow = previousCurrentTerm[0] + 1;
                        repaymentSuccess.postValue("第" + termToShow + "期还款成功");
                        loadOrderDetail(orderId);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "repayLoanOrder onError: " + errorMsg);
                hideLoading();
                errorMessage.postValue(errorMsg);
            }
        });
    }
}
