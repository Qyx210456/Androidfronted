package com.example.androidfronted.viewmodel.loan;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.androidfronted.data.local.entity.ApplicationDetailEntity;
import com.example.androidfronted.data.repository.LoanApplicationRepository;
import com.example.androidfronted.viewmodel.base.BaseViewModel;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDetailViewModel extends BaseViewModel {
    private final LoanApplicationRepository loanApplicationRepository;
    private final MutableLiveData<ApplicationDetailEntity> applicationDetail = new MutableLiveData<>();
    private final MutableLiveData<List<TimelineStep>> timelineSteps = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ApplicationDetailViewModel(@NonNull Application application, LoanApplicationRepository loanApplicationRepository) {
        super(application);
        this.loanApplicationRepository = loanApplicationRepository;
    }

    public MutableLiveData<ApplicationDetailEntity> getApplicationDetail() {
        return applicationDetail;
    }

    public MutableLiveData<List<TimelineStep>> getTimelineSteps() {
        return timelineSteps;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadApplicationDetail(int applicationId) {
        showLoading();
        loanApplicationRepository.getApplicationDetail(applicationId, new LoanApplicationRepository.ApplicationDetailCallback() {
            @Override
            public void onSuccess(ApplicationDetailEntity detail) {
                hideLoading();
                applicationDetail.postValue(detail);
                generateTimelineSteps(detail.getStatus());
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                showError(errorMsg);
            }
        });
    }

    private void generateTimelineSteps(String status) {
        List<TimelineStep> steps = new ArrayList<>();

        // 第一步：提交申请（总是已完成）
        steps.add(new TimelineStep("提交申请", "已提交贷款申请", "", TimelineStatus.COMPLETED));

        // 根据状态生成不同的时间线路径
        if ("AI拒绝".equals(status)) {
            // 路径：提交申请 -> 贷款申请审核中
            steps.add(new TimelineStep("贷款申请审核中", "正在审核中", "", TimelineStatus.ONGOING));
        } else if ("人工拒绝".equals(status)) {
            // 路径：提交申请 -> 贷款申请审核中 -> 审批不通过
            steps.add(new TimelineStep("贷款申请审核中", "已进入审核", "", TimelineStatus.COMPLETED));
            steps.add(new TimelineStep("审批不通过", "贷款申请未通过审批", "", TimelineStatus.COMPLETED));
        } else if ("已通过".equals(status)) {
            // 路径：提交申请 -> 贷款申请审核中 -> 审批通过 -> 打印签订贷款合同 -> 放款成功
            steps.add(new TimelineStep("贷款申请审核中", "已进入审核", "", TimelineStatus.COMPLETED));
            steps.add(new TimelineStep("审批通过", "贷款申请已通过审批", "", TimelineStatus.COMPLETED));
            steps.add(new TimelineStep("打印签订贷款合同", "请打印并签订贷款合同", "", TimelineStatus.ONGOING));
            steps.add(new TimelineStep("放款成功", "贷款已成功发放", "", TimelineStatus.FUTURE));
        } else if ("审核中".equals(status)) {
            // 路径：提交申请 -> 贷款申请审核中
            steps.add(new TimelineStep("贷款申请审核中", "正在审核中", "", TimelineStatus.ONGOING));
        } else if ("已取消".equals(status)) {
            // 路径1：提交申请 -> 已取消
            steps.add(new TimelineStep("已取消", "贷款申请已取消", "", TimelineStatus.COMPLETED));
        } else if ("AI拒绝取消".equals(status)) {
            // 路径2：提交申请 -> 贷款申请审核中 -> 已取消
            steps.add(new TimelineStep("贷款申请审核中", "已进入审核", "", TimelineStatus.COMPLETED));
            steps.add(new TimelineStep("已取消", "贷款申请已取消", "", TimelineStatus.COMPLETED));
        }

        timelineSteps.postValue(steps);
    }

    public int getStatusColor(String status) {
        if ("AI拒绝".equals(status) || "审核中".equals(status)) {
            return com.example.androidfronted.R.color.application_detail_status_corner_pending;
        } else if ("已通过".equals(status)) {
            return com.example.androidfronted.R.color.application_detail_status_corner_approved;
        } else if ("人工拒绝".equals(status)) {
            return com.example.androidfronted.R.color.application_detail_status_corner_rejected;
        } else if ("已取消".equals(status) || "AI拒绝取消".equals(status)) {
            return com.example.androidfronted.R.color.application_detail_status_corner_cancelled;
        }
        return com.example.androidfronted.R.color.application_detail_status_corner_pending;
    }

    public String getStatusText(String status) {
        if ("AI拒绝".equals(status)) {
            return "审核中";
        } else if ("人工拒绝".equals(status)) {
            return "已拒绝";
        } else if ("已通过".equals(status)) {
            return "审批通过";
        } else if ("已取消".equals(status) || "AI拒绝取消".equals(status)) {
            return "已取消";
        }
        return status;
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

    public static class TimelineStep {
        private final String title;
        private final String description;
        private final String time;
        private final TimelineStatus status;

        public TimelineStep(String title, String description, String time, TimelineStatus status) {
            this.title = title;
            this.description = description;
            this.time = time;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getTime() {
            return time;
        }

        public TimelineStatus getStatus() {
            return status;
        }
    }

    public enum TimelineStatus {
        COMPLETED, // 已完成
        ONGOING,   // 进行中
        FUTURE     // 未完成
    }
}