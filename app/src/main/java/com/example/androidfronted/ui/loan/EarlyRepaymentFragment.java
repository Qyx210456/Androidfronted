package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.repository.LoanOrderRepository;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.LoanOrderDetailViewModel;
import java.text.DecimalFormat;
import java.util.List;

public class EarlyRepaymentFragment extends BaseDetailFragment {
    private static final String TAG = "EarlyRepaymentFragment";
    private static final String ARG_ORDER_ID = "order_id";

    public static final String EARLY_REPAYMENT_SUCCESS_REQUEST_KEY = "early_repayment_success_request";
    public static final String EARLY_REPAYMENT_SUCCESS_ORDER_ID = "early_repayment_success_order_id";

    private LoanOrderDetailViewModel viewModel;
    private LoanOrderRepository repository;
    private int orderId;

    private TextView tvRemainingPrincipal;
    private TextView tvInterestRate;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private RadioButton rbPartial;
    private RadioButton rbFull;
    private TextView tvPenaltyFee;
    private TextView tvSavedInterest;
    private TextView tvTotalPayment;
    private Button btnConfirm;
    private ProgressBar progressBar;

    private double remainingPrincipal = 0;
    private double savedInterest = 0;
    private String endDate = "";

    public static EarlyRepaymentFragment newInstance(int orderId) {
        EarlyRepaymentFragment fragment = new EarlyRepaymentFragment();
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
        repository = LoanOrderRepository.getInstance(requireContext());
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_early_repayment, container, false);
        initViews(view);
        observeData();
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvRemainingPrincipal = view.findViewById(R.id.tv_remaining_principal);
        tvInterestRate = view.findViewById(R.id.tv_interest_rate);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        rbPartial = view.findViewById(R.id.rb_partial);
        rbFull = view.findViewById(R.id.rb_full);
        tvPenaltyFee = view.findViewById(R.id.tv_penalty_fee);
        tvSavedInterest = view.findViewById(R.id.tv_saved_interest);
        tvTotalPayment = view.findViewById(R.id.tv_total_payment);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        progressBar = new ProgressBar(requireContext());

        rbFull.setChecked(true);
        rbPartial.setEnabled(false);

        btnConfirm.setOnClickListener(v -> {
            performEarlyRepay();
        });
    }

    private void observeData() {
        viewModel.getOrderDetail().observe(getViewLifecycleOwner(), orderDetail -> {
            if (orderDetail != null) {
                updateLoanInfo(orderDetail);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                btnConfirm.setEnabled(!isLoading);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        viewModel.loadOrderDetail(orderId);
        loadRepaymentPlan();
    }

    private void loadRepaymentPlan() {
        repository.getRepaymentPlan(orderId, 0, new LoanOrderRepository.RepaymentPlanCallback() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                if (plans != null && !plans.isEmpty()) {
                    calculateUnpaidAmounts(plans);
                    findEndDate(plans);
                    updateFeeDisplay();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load repayment plan: " + errorMessage);
            }
        });
    }

    private void updateLoanInfo(LoanOrderDetailEntity detail) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        tvInterestRate.setText(String.format("%.2f%%", detail.getInterestRate()));
        tvStartDate.setText(detail.getStartTime());
    }

    private void calculateUnpaidAmounts(List<RepaymentPlanEntity> plans) {
        savedInterest = 0;
        remainingPrincipal = 0;
        
        for (RepaymentPlanEntity plan : plans) {
            if ("未还".equals(plan.getStatus())) {
                savedInterest += plan.getInterest();
                remainingPrincipal += plan.getPrincipal();
            }
        }
        
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                DecimalFormat df = new DecimalFormat("#,##0.00");
                tvRemainingPrincipal.setText("¥ " + df.format(remainingPrincipal));
                tvTotalPayment.setText("¥ " + df.format(remainingPrincipal));
            });
        }
    }

    private void findEndDate(List<RepaymentPlanEntity> plans) {
        if (plans != null && !plans.isEmpty()) {
            RepaymentPlanEntity lastPlan = plans.get(plans.size() - 1);
            endDate = lastPlan.getDueDate();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvEndDate.setText(endDate);
                });
            }
        }
    }

    private void updateFeeDisplay() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvPenaltyFee.setText("¥ 0.00");
                tvSavedInterest.setText("¥ " + df.format(savedInterest));
                tvTotalPayment.setText("¥ " + df.format(remainingPrincipal));
            });
        }
    }

    private void performEarlyRepay() {
        btnConfirm.setEnabled(false);
        
        repository.earlyRepay(orderId, new LoanOrderRepository.EarlyRepayCallback() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "提前还款成功", Toast.LENGTH_SHORT).show();
                        navigateToSuccess();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true);
                    });
                }
            }
        });
    }

    private void navigateToSuccess() {
        EarlyRepaymentSuccessFragment fragment = EarlyRepaymentSuccessFragment.newInstance(orderId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(((ViewGroup) requireView().getParent()).getId(), fragment)
                .addToBackStack(null)
                .commit();
    }
}
