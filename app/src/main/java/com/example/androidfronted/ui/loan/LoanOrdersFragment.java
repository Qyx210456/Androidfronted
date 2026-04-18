package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.ui.adapter.LoanOrderAdapter;
import com.example.androidfronted.viewmodel.loan.LoanOrdersViewModel;

public class LoanOrdersFragment extends com.example.androidfronted.ui.base.BaseDetailFragment {
    private static final String TAG = "LoanOrdersFragment";

    private LoanOrdersViewModel viewModel;
    private LoanOrderAdapter adapter;

    private RecyclerView recyclerView;
    private TextView tvOrderCount;
    private View containerEmpty;
    private Button btnFilterAll;
    private Button btnFilterNormal;
    private Button btnFilterOverdue;
    private Button btnFilterCompleted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoanOrdersViewModel.class);
        adapter = new LoanOrderAdapter(getContext(), this::onOrderClick);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 底部导航栏的隐藏由 BaseDetailFragment 处理
    }

    @Override
    public void onPause() {
        super.onPause();
        // 底部导航栏的显示由主 Tab 页面的 onResume 处理
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_loan_orders);
        tvOrderCount = view.findViewById(R.id.tv_order_count);
        containerEmpty = view.findViewById(R.id.container_empty);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterNormal = view.findViewById(R.id.btn_filter_normal);
        btnFilterOverdue = view.findViewById(R.id.btn_filter_overdue);
        btnFilterCompleted = view.findViewById(R.id.btn_filter_completed);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && getContext() != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoanOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                adapter.setOrderList(orders);
                updateEmptyState(orders.isEmpty());
                if (getContext() != null) {
                    String text = getString(R.string.loan_order_count, orders.size());
                    tvOrderCount.setText(text);
                }
            }
        });
    }

    private void setupClickListeners() {
        btnFilterAll.setOnClickListener(v -> filterOrders("全部"));
        btnFilterNormal.setOnClickListener(v -> filterOrders("正常"));
        btnFilterOverdue.setOnClickListener(v -> filterOrders("已逾期"));
        btnFilterCompleted.setOnClickListener(v -> filterOrders("已完成"));
    }

    private void loadData() {
        viewModel.loadLoanOrders();
    }

    private void filterOrders(String status) {
        updateFilterButtons(status);
        viewModel.filterLoanOrdersByStatus(status);
    }

    private void updateFilterButtons(String currentFilter) {
        resetAllFilterButtons();

        Button selectedBtn = null;
        if ("全部".equals(currentFilter)) {
            selectedBtn = btnFilterAll;
        } else if ("正常".equals(currentFilter)) {
            selectedBtn = btnFilterNormal;
        } else if ("已逾期".equals(currentFilter)) {
            selectedBtn = btnFilterOverdue;
        } else if ("已完成".equals(currentFilter)) {
            selectedBtn = btnFilterCompleted;
        }

        if (selectedBtn != null) {
            selectedBtn.setSelected(true);
        }
    }

    private void resetAllFilterButtons() {
        btnFilterAll.setSelected(false);
        btnFilterNormal.setSelected(false);
        btnFilterOverdue.setSelected(false);
        btnFilterCompleted.setSelected(false);
    }

    private void onOrderClick(LoanOrderEntity order) {
        LoanOrderDetailFragment detailFragment = LoanOrderDetailFragment.newInstance(order.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            containerEmpty.setVisibility(View.VISIBLE);
            tvOrderCount.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            containerEmpty.setVisibility(View.GONE);
            tvOrderCount.setVisibility(View.VISIBLE);
        }
    }
}
