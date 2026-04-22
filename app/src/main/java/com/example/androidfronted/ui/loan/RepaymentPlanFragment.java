package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.adapter.RepaymentPlanAdapter;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.RepaymentPlanViewModel;

public class RepaymentPlanFragment extends BaseDetailFragment {
    private static final String ARG_ORDER_ID = "order_id";

    private RepaymentPlanViewModel viewModel;
    private RepaymentPlanAdapter adapter;
    private int orderId;

    private TextView tvTermInfo;
    private TextView tvRepaidCount;
    private TextView tvUnpaidCount;
    private TextView tvOverdueCount;
    private RecyclerView rvRepaymentPlan;
    private Button btnFilterAll;
    private Button btnFilterRepaid;
    private Button btnFilterUnpaid;
    private Button btnFilterOverdue;

    public static RepaymentPlanFragment newInstance(int orderId) {
        RepaymentPlanFragment fragment = new RepaymentPlanFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(RepaymentPlanViewModel.class);
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
        View view = inflater.inflate(R.layout.fragment_repayment_plan, container, false);
        initViews(view);
        initRecyclerView();
        initFilterButtons();
        observeData();
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvTermInfo = view.findViewById(R.id.tv_term_info);
        tvRepaidCount = view.findViewById(R.id.tv_repaid_count);
        tvUnpaidCount = view.findViewById(R.id.tv_unpaid_count);
        tvOverdueCount = view.findViewById(R.id.tv_overdue_count);
        rvRepaymentPlan = view.findViewById(R.id.rv_repayment_plan);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterRepaid = view.findViewById(R.id.btn_filter_repaid);
        btnFilterUnpaid = view.findViewById(R.id.btn_filter_unpaid);
        btnFilterOverdue = view.findViewById(R.id.btn_filter_overdue);

        ImageView btnBack = view.findViewById(R.id.apply_btn_back);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void initRecyclerView() {
        adapter = new RepaymentPlanAdapter(requireContext());
        rvRepaymentPlan.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRepaymentPlan.setAdapter(adapter);
    }

    private void initFilterButtons() {
        btnFilterAll.setSelected(true);

        btnFilterAll.setOnClickListener(v -> onFilterClicked(btnFilterAll, "全部"));
        btnFilterRepaid.setOnClickListener(v -> onFilterClicked(btnFilterRepaid, "已还"));
        btnFilterUnpaid.setOnClickListener(v -> onFilterClicked(btnFilterUnpaid, "未还"));
        btnFilterOverdue.setOnClickListener(v -> onFilterClicked(btnFilterOverdue, "逾期"));
    }

    private void onFilterClicked(Button selectedButton, String filterType) {
        btnFilterAll.setSelected(false);
        btnFilterRepaid.setSelected(false);
        btnFilterUnpaid.setSelected(false);
        btnFilterOverdue.setSelected(false);

        selectedButton.setSelected(true);
        viewModel.filterByStatus(filterType);
    }

    private void observeData() {
        viewModel.getFilteredPlans().observe(getViewLifecycleOwner(), plans -> {
            if (plans != null) {
                adapter.setPlanList(plans);
            }
        });

        viewModel.getTermInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                tvTermInfo.setText(info);
            }
        });

        viewModel.getRepaidCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvRepaidCount.setText(count + "期");
            }
        });

        viewModel.getUnpaidCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvUnpaidCount.setText(count + "期");
            }
        });

        viewModel.getOverdueCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvOverdueCount.setText(count + "期");
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        viewModel.loadRepaymentPlan(orderId);
    }

    private void refreshData() {
        viewModel.refreshRepaymentPlan(orderId);
    }
}
