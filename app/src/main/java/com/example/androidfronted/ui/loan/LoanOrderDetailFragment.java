package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
    private int orderId;

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
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvUnpaidTotal;
    private TextView tvUnpaidPrincipal;
    private TextView tvUnpaidInterest;

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
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID, 0);
        }
        
        getParentFragmentManager().setFragmentResultListener(
                PaymentSuccessFragment.PAYMENT_SUCCESS_REQUEST_KEY,
                this,
                (requestKey, bundle) -> {
                    int returnedOrderId = bundle.getInt(PaymentSuccessFragment.PAYMENT_SUCCESS_ORDER_ID, 0);
                    if (returnedOrderId == orderId) {
                        refreshData();
                    }
                }
        );
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
        progressBar = view.findViewById(R.id.progress_bar);
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        tvUnpaidTotal = view.findViewById(R.id.tv_unpaid_total);
        tvUnpaidPrincipal = view.findViewById(R.id.tv_unpaid_principal);
        tvUnpaidInterest = view.findViewById(R.id.tv_unpaid_interest);
        
        view.findViewById(R.id.btnrepaid).setOnClickListener(v -> {
            PaymentFragment fragment = PaymentFragment.newInstance(orderId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(((ViewGroup) requireView().getParent()).getId(), fragment)
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btn_repayment_plan).setOnClickListener(v -> {
            RepaymentPlanFragment fragment = RepaymentPlanFragment.newInstance(orderId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(((ViewGroup) requireView().getParent()).getId(), fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void observeData() {
        viewModel.getOrderDetail().observe(getViewLifecycleOwner(), orderDetail -> {
            if (orderDetail != null) {
                updateUI(orderDetail);
            }
        });

        viewModel.getTotalLoanAmount().observe(getViewLifecycleOwner(), totalAmount -> {
            if (totalAmount != null) {
                tvLoanAmountValue.setText(viewModel.formatAmount(totalAmount));
            }
        });

        viewModel.getProgressPercent().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null && progressBar != null) {
                progressBar.setProgress(progress);
            }
        });

        viewModel.getProgressText().observe(getViewLifecycleOwner(), text -> {
            if (text != null && tvProgressPercent != null) {
                tvProgressPercent.setText(text);
            }
        });

        viewModel.getUnpaidTotal().observe(getViewLifecycleOwner(), total -> {
            if (total != null && tvUnpaidTotal != null) {
                tvUnpaidTotal.setText(viewModel.formatAmount(total));
            }
        });

        viewModel.getUnpaidPrincipal().observe(getViewLifecycleOwner(), principal -> {
            if (principal != null && tvUnpaidPrincipal != null) {
                tvUnpaidPrincipal.setText(viewModel.formatAmount(principal));
            }
        });

        viewModel.getUnpaidInterest().observe(getViewLifecycleOwner(), interest -> {
            if (interest != null && tvUnpaidInterest != null) {
                tvUnpaidInterest.setText(viewModel.formatAmount(interest));
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRepaymentSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getNavigationType() == com.example.androidfronted.viewmodel.base.NavigationEvent.NAVIGATE_BACK) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void loadData() {
        viewModel.loadOrderDetail(orderId);
    }

    private void refreshData() {
        viewModel.refreshOrderDetail(orderId);
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
