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
import java.util.ArrayList;
import java.util.List;

/**
 * 还款计划页面ViewModel
 */
public class RepaymentPlanViewModel extends BaseViewModel {
    private static final String TAG = "RepaymentPlanVM";
    private final LoanOrderRepository loanOrderRepository;
    
    private final MutableLiveData<List<RepaymentPlanEntity>> allPlans = new MutableLiveData<>();
    private final MutableLiveData<List<RepaymentPlanEntity>> filteredPlans = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("全部");
    private final MutableLiveData<String> termInfo = new MutableLiveData<>("分0期还款");
    private final MutableLiveData<Integer> repaidCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> unpaidCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> overdueCount = new MutableLiveData<>(0);

    public RepaymentPlanViewModel(@NonNull Application application, LoanOrderRepository loanOrderRepository) {
        super(application);
        this.loanOrderRepository = loanOrderRepository;
    }

    public MutableLiveData<List<RepaymentPlanEntity>> getAllPlans() {
        return allPlans;
    }

    public MutableLiveData<List<RepaymentPlanEntity>> getFilteredPlans() {
        return filteredPlans;
    }

    public MutableLiveData<String> getCurrentFilter() {
        return currentFilter;
    }

    public MutableLiveData<String> getTermInfo() {
        return termInfo;
    }

    public MutableLiveData<Integer> getRepaidCount() {
        return repaidCount;
    }

    public MutableLiveData<Integer> getUnpaidCount() {
        return unpaidCount;
    }

    public MutableLiveData<Integer> getOverdueCount() {
        return overdueCount;
    }

    /**
     * 加载还款计划（从网络获取）
     * 先获取订单详情得到currentTerm，再获取还款计划
     */
    public void loadRepaymentPlan(int orderId) {
        showLoading();
        Log.d(TAG, "loadRepaymentPlan for orderId: " + orderId);
        
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    int currentTerm = detail.getCurrentTerm();
                    int totalTerm = detail.getTerm();
                    Log.d(TAG, "Got currentTerm: " + currentTerm + " from order detail");
                    termInfo.postValue("分" + totalTerm + "期还款");
                    loadRepaymentPlanWithCurrentTerm(orderId, currentTerm);
                } else {
                    hideLoading();
                    showError("获取订单详情失败");
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Failed to get order detail: " + errorMsg);
                loadRepaymentPlanWithCurrentTerm(orderId, 0);
            }
        });
    }

    /**
     * 加载还款计划（从本地获取，用于刷新）
     */
    public void loadRepaymentPlanFromLocal(int orderId) {
        Log.d(TAG, "loadRepaymentPlanFromLocal for orderId: " + orderId);
        
        loanOrderRepository.getLoanOrderDetailFromLocal(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    int currentTerm = detail.getCurrentTerm();
                    int totalTerm = detail.getTerm();
                    Log.d(TAG, "Got currentTerm from local: " + currentTerm);
                    termInfo.postValue("分" + totalTerm + "期还款");
                    loadRepaymentPlanWithCurrentTerm(orderId, currentTerm);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Failed to get order detail from local: " + errorMsg);
            }
        });
    }

    /**
     * 刷新还款计划（先本地加载，再网络请求更新）
     */
    public void refreshRepaymentPlan(int orderId) {
        Log.d(TAG, "refreshRepaymentPlan for orderId: " + orderId);
        
        loanOrderRepository.getLoanOrderDetailFromLocal(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    int currentTerm = detail.getCurrentTerm();
                    int totalTerm = detail.getTerm();
                    Log.d(TAG, "Got currentTerm from local for refresh: " + currentTerm);
                    termInfo.postValue("分" + totalTerm + "期还款");
                    loadRepaymentPlanWithCurrentTerm(orderId, currentTerm);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Failed to get order detail from local: " + errorMsg);
            }
        });
        
        loanOrderRepository.getLoanOrderDetail(orderId, new LoanOrderRepository.LoanOrderDetailCallback() {
            @Override
            public void onSuccess(LoanOrderDetailEntity detail) {
                if (detail != null) {
                    int currentTerm = detail.getCurrentTerm();
                    int totalTerm = detail.getTerm();
                    Log.d(TAG, "Got currentTerm from network for refresh: " + currentTerm);
                    termInfo.postValue("分" + totalTerm + "期还款");
                    loadRepaymentPlanWithCurrentTermSilent(orderId, currentTerm);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Failed to get order detail from network: " + errorMsg);
            }
        });
    }

    /**
     * 根据currentTerm加载还款计划（静默加载，不显示loading）
     */
    private void loadRepaymentPlanWithCurrentTermSilent(int orderId, int currentTerm) {
        loanOrderRepository.getRepaymentPlan(orderId, currentTerm, new LoanOrderRepository.RepaymentPlanCallback() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                Log.d(TAG, "Got " + (plans != null ? plans.size() : 0) + " repayment plans (silent)");
                allPlans.postValue(plans);
                calculateTermStats(plans);
                applyFilter(plans, currentFilter.getValue());
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load repayment plans (silent): " + errorMessage);
            }
        });
    }

    /**
     * 根据currentTerm加载还款计划
     */
    private void loadRepaymentPlanWithCurrentTerm(int orderId, int currentTerm) {
        loanOrderRepository.getRepaymentPlan(orderId, currentTerm, new LoanOrderRepository.RepaymentPlanCallback() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                hideLoading();
                Log.d(TAG, "Got " + (plans != null ? plans.size() : 0) + " repayment plans");
                allPlans.postValue(plans);
                calculateTermStats(plans);
                applyFilter(plans, currentFilter.getValue());
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                showError(errorMessage);
            }
        });
    }

    /**
     * 根据状态筛选
     */
    public void filterByStatus(String status) {
        currentFilter.postValue(status);
        List<RepaymentPlanEntity> plans = allPlans.getValue();
        if (plans != null) {
            applyFilter(plans, status);
        }
    }

    /**
     * 应用筛选
     */
    private void applyFilter(List<RepaymentPlanEntity> plans, String status) {
        if (plans == null) {
            filteredPlans.postValue(new ArrayList<>());
            return;
        }

        if ("全部".equals(status)) {
            filteredPlans.postValue(plans);
        } else {
            List<RepaymentPlanEntity> filtered = new ArrayList<>();
            for (RepaymentPlanEntity plan : plans) {
                if (status.equals(plan.getStatus())) {
                    filtered.add(plan);
                }
            }
            filteredPlans.postValue(filtered);
        }
    }

    /**
     * 计算期数统计信息
     */
    private void calculateTermStats(List<RepaymentPlanEntity> plans) {
        if (plans == null || plans.isEmpty()) {
            repaidCount.postValue(0);
            unpaidCount.postValue(0);
            overdueCount.postValue(0);
            return;
        }

        int repaid = 0;
        int unpaid = 0;
        int overdue = 0;

        for (RepaymentPlanEntity plan : plans) {
            String status = plan.getStatus();
            if ("已还".equals(status)) {
                repaid++;
            } else if ("未还".equals(status)) {
                unpaid++;
            } else if ("逾期".equals(status)) {
                overdue++;
            }
        }

        repaidCount.postValue(repaid);
        unpaidCount.postValue(unpaid);
        overdueCount.postValue(overdue);
    }

    /**
     * 格式化金额显示
     */
    public String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "¥" + df.format(amount);
    }
}
