package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.LoanOrderDetailViewModel;
import java.text.DecimalFormat;

public class LoanOrderDetailFragment extends BaseDetailFragment {
    private static final String ARG_ORDER_ID = "order_id";

    private LoanOrderDetailViewModel viewModel;

    private TextView tvLoanName;
    private TextView tvOrderStatus;
    private TextView tvOrderNumberValue;
    private TextView tvLoanAmountValue;
    private TextView tvRepaidAmountValue;
    private TextView tvCurrentTerm;
    private TextView tvOverdueDays;
    private TextView tvStartTime;
    private TextView tvRepaidType;
    private TextView tvLoanPeriod;
    private TextView tvTerm;
    private TextView tvInterestRate;

    public static LoanOrderDetailFragment newInstance(int orderId) {
        LoanOrderDetailFragment fragment = new LoanOrderDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(LoanOrderDetailViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loan_order_detail, container, false);
        initViews(view);
        observeData();
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvLoanName = view.findViewById(R.id.tv_loan_name);
        tvOrderStatus = view.findViewById(R.id.tv_order_status);
        tvOrderNumberValue = view.findViewById(R.id.tv_order_number_value);
        tvLoanAmountValue = view.findViewById(R.id.tv_loan_amount_value);
        tvRepaidAmountValue = view.findViewById(R.id.tv_repaid_amount_value);
        tvCurrentTerm = view.findViewById(R.id.tv_current_term);
        tvOverdueDays = view.findViewById(R.id.tv_overdue_days);
        tvStartTime = view.findViewById(R.id.tv_start_time);
        tvRepaidType = view.findViewById(R.id.tv_repaid_type);
        tvLoanPeriod = view.findViewById(R.id.tv_loan_period);
        tvTerm = view.findViewById(R.id.tv_term);
        tvInterestRate = view.findViewById(R.id.tv_interest_rate);
        
        // 还款按钮点击事件
        view.findViewById(R.id.btnrepaid).setOnClickListener(v -> {
            if (getArguments() != null) {
                int orderId = getArguments().getInt(ARG_ORDER_ID, 0);
                viewModel.repayLoanOrder(orderId);
            }
        });
    }

    private void observeData() {
        viewModel.getOrderDetail().observe(getViewLifecycleOwner(), orderDetail -> {
            if (orderDetail != null) {
                updateUI(orderDetail);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
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
            int orderId = getArguments().getInt(ARG_ORDER_ID, 0);
            viewModel.loadOrderDetail(orderId);
        }
    }

    private void updateUI(LoanOrderDetailEntity detail) {
        tvLoanName.setText(detail.getProductName());

        String status = detail.getStatus();
        tvOrderStatus.setText(status);
        tvOrderStatus.setBackgroundResource(viewModel.getStatusBackground(status));
        tvOrderStatus.setTextColor(requireContext().getResources().getColor(viewModel.getStatusTextColor(status)));

        String orderNumber = viewModel.generateOrderNumber(detail);
        tvOrderNumberValue.setText(orderNumber);

        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvLoanAmountValue.setText("¥" + df.format(detail.getLoanAmount()));
        tvRepaidAmountValue.setText("¥" + df.format(detail.getRepaidAmount()));

        String currentTermText = viewModel.formatCurrentTerm(detail);
        tvCurrentTerm.setText(currentTermText);

        tvOverdueDays.setText(detail.getOverdueDays() + " 天");

        tvStartTime.setText(detail.getStartTime());

        tvRepaidType.setText(detail.getRepaidType());

        String loanPeriodText = viewModel.formatLoanPeriod(detail.getLoanPeriod());
        tvLoanPeriod.setText(loanPeriodText);

        tvTerm.setText(detail.getTerm() + "期");

        String interestRateText = viewModel.formatInterestRate(detail.getInterestRate());
        tvInterestRate.setText(interestRateText);
    }
}
