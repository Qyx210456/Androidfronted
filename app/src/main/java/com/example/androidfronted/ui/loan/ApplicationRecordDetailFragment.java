package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.adapter.ApplicationTimelineAdapter;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.ApplicationDetailViewModel;
import java.text.DecimalFormat;

public class ApplicationRecordDetailFragment extends BaseDetailFragment {
    private static final String ARG_APPLICATION_ID = "application_id";
    private static final String ARG_PRODUCT_NAME = "product_name";

    private ApplicationDetailViewModel viewModel;
    private ApplicationTimelineAdapter adapter;

    private TextView tvLoanName;
    private TextView tvStatusLabel;
    private View applicationDetailCard;
    private TextView tvLoanAmount;
    private TextView tvLoanPeriod;
    private TextView tvTerm;
    private TextView tvInterestRate;
    private TextView tvRepaidType;
    private TextView tvApplyTime;
    private RecyclerView recyclerViewTimeline;
    private ImageView btnBack;

    public static ApplicationRecordDetailFragment newInstance(int applicationId, String productName) {
        ApplicationRecordDetailFragment fragment = new ApplicationRecordDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_APPLICATION_ID, applicationId);
        args.putString(ARG_PRODUCT_NAME, productName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(ApplicationDetailViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application_record_detail, container, false);
        initViews(view);
        setupRecyclerView();
        observeData();
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvLoanName = view.findViewById(R.id.tv_loan_name);
        tvStatusLabel = view.findViewById(R.id.tv_status_label);
        applicationDetailCard = view.findViewById(R.id.application_detail_card);
        tvLoanAmount = view.findViewById(R.id.tv_loan_amount);
        tvLoanPeriod = view.findViewById(R.id.tv_loan_period);
        tvTerm = view.findViewById(R.id.tv_term);
        tvInterestRate = view.findViewById(R.id.tv_interest_rate);
        tvRepaidType = view.findViewById(R.id.tv_repaid_type);
        tvApplyTime = view.findViewById(R.id.tv_apply_time);
        recyclerViewTimeline = view.findViewById(R.id.recycler_timeline);
        btnBack = view.findViewById(R.id.apply_btn_back);

        btnBack.setOnClickListener(v -> viewModel.navigateBack());
    }

    private void setupRecyclerView() {
        adapter = new ApplicationTimelineAdapter(null);
        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTimeline.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getApplicationDetail().observe(getViewLifecycleOwner(), applicationDetail -> {
            if (applicationDetail != null) {
                updateUI(applicationDetail);
            }
        });

        viewModel.getTimelineSteps().observe(getViewLifecycleOwner(), steps -> {
            if (steps != null) {
                adapter.updateData(steps);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // 可以添加加载状态的处理
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getNavigationType() == com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void loadData() {
        if (getArguments() != null) {
            int applicationId = getArguments().getInt(ARG_APPLICATION_ID, 0);
            viewModel.loadApplicationDetail(applicationId);
        }
    }

    private void updateUI(com.example.androidfronted.data.local.entity.ApplicationDetailEntity detail) {
        // 设置贷款名称
        if (getArguments() != null) {
            String productName = getArguments().getString(ARG_PRODUCT_NAME, "");
            tvLoanName.setText(productName);
        }

        // 设置状态
        String status = detail.getStatus();
        tvStatusLabel.setText(viewModel.getStatusText(status));
        int statusColor = viewModel.getStatusColor(status);
        tvStatusLabel.setBackgroundColor(requireContext().getResources().getColor(statusColor));

        // 设置贷款金额
        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvLoanAmount.setText("¥" + df.format(detail.getLoanAmount()));

        // 设置贷款年限
        tvLoanPeriod.setText(detail.getLoanPeriod() + "年");

        // 设置还款期数
        tvTerm.setText(detail.getTerm() + "期");

        // 设置年化利率
        df.applyPattern("#.##");
        tvInterestRate.setText(df.format(detail.getInterestRate() * 100) + "%");

        // 设置还款方式
        tvRepaidType.setText(detail.getRepaidType());

        // 设置申请时间
        tvApplyTime.setText(detail.getApplyTime());
    }
}