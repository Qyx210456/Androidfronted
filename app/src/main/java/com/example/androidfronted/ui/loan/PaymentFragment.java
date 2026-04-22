package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.PaymentViewModel;

public class PaymentFragment extends Fragment {
    private static final String ARG_ORDER_ID = "order_id";

    private PaymentViewModel viewModel;

    private TextView tvAmount;
    private TextView tvLoanName;
    private TextView tvTerm;
    private LinearLayout itemAlipay;
    private LinearLayout itemWechat;
    private LinearLayout itemCloudQuickpass;
    private ImageView ivAlipayCheck;
    private ImageView ivWechatCheck;
    private ImageView ivCloudCheck;
    private TextView btnPayNow;

    private String selectedPaymentMethod = null;
    private int orderId;
    private int currentTerm;

    public static PaymentFragment newInstance(int orderId) {
        PaymentFragment fragment = new PaymentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(PaymentViewModel.class);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repayment, container, false);
        initViews(view);
        observeData();
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvAmount = view.findViewById(R.id.tv_amount);
        tvLoanName = view.findViewById(R.id.tv_loan_name);
        tvTerm = view.findViewById(R.id.tv_term);
        itemAlipay = view.findViewById(R.id.item_alipay);
        itemWechat = view.findViewById(R.id.item_wechat);
        itemCloudQuickpass = view.findViewById(R.id.item_cloud_quickpass);
        ivAlipayCheck = view.findViewById(R.id.iv_alipay_check);
        ivWechatCheck = view.findViewById(R.id.iv_wechat_check);
        ivCloudCheck = view.findViewById(R.id.iv_cloud_check);
        btnPayNow = view.findViewById(R.id.btn_pay_now);

        view.findViewById(R.id.apply_btn_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        itemAlipay.setOnClickListener(v -> selectPaymentMethod("alipay"));
        itemWechat.setOnClickListener(v -> selectPaymentMethod("wechat"));
        itemCloudQuickpass.setOnClickListener(v -> selectPaymentMethod("cloud"));

        btnPayNow.setOnClickListener(v -> {
            viewModel.executePayment();
        });

        updatePaymentMethodUI();
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        viewModel.setSelectedPaymentMethod(method);
        updatePaymentMethodUI();
    }

    private void updatePaymentMethodUI() {
        ivAlipayCheck.setSelected("alipay".equals(selectedPaymentMethod));
        ivWechatCheck.setSelected("wechat".equals(selectedPaymentMethod));
        ivCloudCheck.setSelected("cloud".equals(selectedPaymentMethod));
    }

    private void observeData() {
        viewModel.getOrderDetail().observe(getViewLifecycleOwner(), detail -> {
            if (detail != null) {
                tvLoanName.setText(detail.getProductName());
                currentTerm = detail.getCurrentTerm();
            }
        });

        viewModel.getCurrentTermPlan().observe(getViewLifecycleOwner(), plan -> {
            if (plan != null) {
                tvAmount.setText(viewModel.formatAmount(plan.getTotal()));
                int term = currentTerm + 1;
                tvTerm.setText(getString(R.string.payment_term_format, term));
            }
        });

        viewModel.getPaymentSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                navigateToSuccess();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                btnPayNow.setEnabled(!isLoading);
            }
        });
    }

    private void loadData() {
        if (getArguments() != null) {
            int orderId = getArguments().getInt(ARG_ORDER_ID, 0);
            viewModel.loadPaymentData(orderId);
        }
    }

    private void navigateToSuccess() {
        int term = currentTerm + 1;
        PaymentSuccessFragment fragment = PaymentSuccessFragment.newInstance(orderId, term);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(((ViewGroup) requireView().getParent()).getId(), fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
