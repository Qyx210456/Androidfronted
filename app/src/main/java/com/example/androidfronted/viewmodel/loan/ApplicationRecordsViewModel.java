package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import com.example.androidfronted.data.repository.LoanApplicationRepository;
import com.example.androidfronted.data.repository.UserRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import java.util.ArrayList;
import java.util.List;

public class ApplicationRecordsViewModel extends BaseViewModel {
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    
    private final MutableLiveData<List<ApplicationEntity>> applications = new MutableLiveData<>();
    private final MutableLiveData<List<ApplicationEntity>> filteredApplications = new MutableLiveData<>();
    private final MutableLiveData<Integer> recordCount = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>();
    
    public static final String FILTER_ALL = "全部";
    public static final String FILTER_PENDING = "审核中";
    public static final String FILTER_APPROVED = "审批通过";
    public static final String FILTER_REJECTED = "已拒绝";
    public static final String FILTER_CANCELLED = "已取消";

    public ApplicationRecordsViewModel(@NonNull Application application) {
        super(application);
        this.loanApplicationRepository = LoanApplicationRepository.getInstance(application);
        this.userRepository = UserRepository.getInstance(application);
        this.currentFilter.setValue(FILTER_ALL);
        
        // 监听applications变化，自动应用筛选
        applications.observeForever(applicationList -> {
            if (applicationList != null) {
                applyFilter(currentFilter.getValue());
            }
        });
    }

    public MutableLiveData<List<ApplicationEntity>> getApplications() {
        return applications;
    }

    public MutableLiveData<List<ApplicationEntity>> getFilteredApplications() {
        return filteredApplications;
    }

    public MutableLiveData<Integer> getRecordCount() {
        return recordCount;
    }

    public MutableLiveData<String> getCurrentFilter() {
        return currentFilter;
    }

    public void loadApplications() {
        showLoading();
        
        loanApplicationRepository.getMyApplications(new LoanApplicationRepository.ApplicationsCallback() {
            @Override
            public void onSuccess(List<ApplicationEntity> applicationList) {
                hideLoading();
                applications.postValue(applicationList);
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    public void setFilter(String filter) {
        currentFilter.setValue(filter);
        applyFilter(filter);
    }

    private void applyFilter(String filter) {
        List<ApplicationEntity> sourceList = applications.getValue();
        if (sourceList == null) {
            return;
        }

        List<ApplicationEntity> filteredList = new ArrayList<>();
        
        if (FILTER_ALL.equals(filter)) {
            filteredList.addAll(sourceList);
        } else if (FILTER_REJECTED.equals(filter)) {
            for (ApplicationEntity entity : sourceList) {
                String status = entity.getStatus();
                if ("人工拒绝".equals(status)) {
                    filteredList.add(entity);
                }
            }
        } else if (FILTER_APPROVED.equals(filter)) {
            for (ApplicationEntity entity : sourceList) {
                if ("已通过".equals(entity.getStatus())) {
                    filteredList.add(entity);
                }
            }
        } else if (FILTER_PENDING.equals(filter)) {
            for (ApplicationEntity entity : sourceList) {
                String status = entity.getStatus();
                if ("审核中".equals(status) || "AI拒绝".equals(status)) {
                    filteredList.add(entity);
                }
            }
        } else if (FILTER_CANCELLED.equals(filter)) {
            for (ApplicationEntity entity : sourceList) {
                if ("已取消".equals(entity.getStatus())) {
                    filteredList.add(entity);
                }
            }
        }

        filteredApplications.postValue(filteredList);
        recordCount.postValue(filteredList.size());
    }

    public String getDisplayStatus(String backendStatus) {
        if ("人工拒绝".equals(backendStatus)) {
            return "已拒绝";
        } else if ("已通过".equals(backendStatus)) {
            return "审批通过";
        } else if ("AI拒绝".equals(backendStatus)) {
            return "审核中";
        }
        return backendStatus;
    }

    public boolean isPendingStatus(String status) {
        return "审核中".equals(status) || "AI拒绝".equals(status);
    }

    public void navigateBack() {
        navigate(com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK);
    }

    public void withdrawApplication(int applicationId, LoanApplicationRepository.CallbackResult callback) {
        showLoading();
        loanApplicationRepository.withdrawApplication(applicationId, new LoanApplicationRepository.CallbackResult() {
            @Override
            public void onSuccess(String successMessage) {
                hideLoading();
                callback.onSuccess(successMessage);
                // 重新加载申请列表，更新状态
                loadApplications();
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                showError(errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
}
