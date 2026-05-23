package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.ApplyDeferViewModel;

public class ApplyDeferFragment extends BaseDetailFragment {
    private static final String ARG_ORDER_ID = "order_id";

    private ApplyDeferViewModel viewModel;
    private int orderId;

    private ImageView btnBack;
    private TextView tvCurrentTotal;
    private TextView tvCurrentPrincipal;
    private TextView tvCurrentInterest;
    private TextView tvDueDate;
    private TextView tvCurrentTerm;
    private TextView tvRemainingTerms;
    private TextView tvRemainingTotal;
    private TextView tvRemainingPrincipal;
    private TextView tvRemainingInterest;
    private FrameLayout reasonSelector;
    private TextView tvSelectedReason;
    private LinearLayout optionCard;
    private RadioButton rbOption;
    private TextView tvFee;
    private TextView tvExtraInterest;
    private TextView tvNextPayment;
    private CheckBox cbAgree;
    private TextView tvAgreement;
    private Button btnSubmit;

    private String selectedReason = null;
    private int selectedReasonPosition = -1;

    private final String[] deferReasons = {
            "临时资金周转困难",
            "收入临时性下降",
            "失业/待业",
            "疾病/医疗支出",
            "家庭变故",
            "自然灾害影响",
            "其他"
    };

    public static ApplyDeferFragment newInstance(int orderId) {
        ApplyDeferFragment fragment = new ApplyDeferFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(ApplyDeferViewModel.class);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply_defer, container, false);
        initViews(view);
        setupAgreementLink();
        observeData();
        loadData();
        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        tvCurrentTotal = view.findViewById(R.id.tv_current_total);
        tvCurrentPrincipal = view.findViewById(R.id.tv_current_principal);
        tvCurrentInterest = view.findViewById(R.id.tv_current_interest);
        tvDueDate = view.findViewById(R.id.tv_due_date);
        tvCurrentTerm = view.findViewById(R.id.tv_current_term);
        tvRemainingTerms = view.findViewById(R.id.tv_remaining_terms);
        tvRemainingTotal = view.findViewById(R.id.tv_remaining_total);
        tvRemainingPrincipal = view.findViewById(R.id.tv_remaining_principal);
        tvRemainingInterest = view.findViewById(R.id.tv_remaining_interest);
        reasonSelector = view.findViewById(R.id.reason_selector);
        tvSelectedReason = view.findViewById(R.id.tv_selected_reason);
        optionCard = view.findViewById(R.id.option_card);
        rbOption = view.findViewById(R.id.rb_option);
        tvFee = view.findViewById(R.id.tv_fee);
        tvExtraInterest = view.findViewById(R.id.tv_extra_interest);
        tvNextPayment = view.findViewById(R.id.tv_next_payment);
        cbAgree = view.findViewById(R.id.cb_agree);
        tvAgreement = view.findViewById(R.id.tv_agreement);
        btnSubmit = view.findViewById(R.id.btn_submit);

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        reasonSelector.setOnClickListener(v -> {
            showReasonBottomSheet();
        });

        optionCard.setOnClickListener(v -> {
            toggleOptionSelection();
        });

        rbOption.setOnClickListener(v -> {
            toggleOptionSelection();
        });

        btnSubmit.setOnClickListener(v -> {
            handleSubmit();
        });
    }

    private void showReasonBottomSheet() {
        DeferReasonBottomSheet bottomSheet = DeferReasonBottomSheet.newInstance(deferReasons, selectedReasonPosition);
        bottomSheet.setOnReasonSelectedListener((reason, position) -> {
            selectedReason = reason;
            tvSelectedReason.setText(reason);
            tvSelectedReason.setTextColor(getResources().getColor(R.color.defer_dark_blue));
            selectedReasonPosition = position;
        });
        bottomSheet.show(getChildFragmentManager(), "DeferReasonBottomSheet");
    }

    private void setupAgreementLink() {
        String fullText = "我已阅读并同意《延期还款协议》";
        SpannableString spannableString = new SpannableString(fullText);
        
        int start = fullText.indexOf("《");
        int end = fullText.indexOf("》") + 1;
        
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(requireContext(), "协议页面开发中", Toast.LENGTH_SHORT).show();
            }
        };
        
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvAgreement.setText(spannableString);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void observeData() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                btnSubmit.setEnabled(!isLoading);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSubmitSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                navigateToSuccess();
            }
        });

        viewModel.getCurrentTotalAmount().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvCurrentTotal.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getCurrentPrincipal().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvCurrentPrincipal.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getCurrentInterest().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvCurrentInterest.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getDueDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                tvDueDate.setText(date);
            }
        });

        viewModel.getCurrentTerm().observe(getViewLifecycleOwner(), term -> {
            if (term != null) {
                tvCurrentTerm.setText(term);
            }
        });

        viewModel.getRemainingTerms().observe(getViewLifecycleOwner(), terms -> {
            if (terms != null) {
                tvRemainingTerms.setText(terms + " 期");
            }
        });

        viewModel.getRemainingTotal().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvRemainingTotal.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getRemainingPrincipal().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvRemainingPrincipal.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getRemainingInterest().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvRemainingInterest.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getDeferFee().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvFee.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getExtraInterest().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvExtraInterest.setText(viewModel.formatAmount(amount));
            }
        });

        viewModel.getNextPayment().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null) {
                tvNextPayment.setText(viewModel.formatAmount(amount));
            }
        });
    }

    private void toggleOptionSelection() {
        boolean isSelected = !optionCard.isSelected();
        optionCard.setSelected(isSelected);
        rbOption.setChecked(isSelected);
    }

    private void loadData() {
        viewModel.loadData(orderId);
    }

    private void handleSubmit() {
        if (selectedReason == null) {
            Toast.makeText(requireContext(), "请选择延期原因", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!optionCard.isSelected()) {
            Toast.makeText(requireContext(), "请选择延期方案", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAgree.isChecked()) {
            Toast.makeText(requireContext(), "请阅读并同意延期还款协议", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.submitPostpone(orderId);
    }

    private void navigateToSuccess() {
        ApplyDeferSuccessFragment fragment = ApplyDeferSuccessFragment.newInstance(orderId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(((ViewGroup) requireView().getParent()).getId(), fragment)
                .addToBackStack(null)
                .commit();
    }
}
