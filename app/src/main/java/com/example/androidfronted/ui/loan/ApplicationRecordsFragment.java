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
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import com.example.androidfronted.data.repository.LoanApplicationRepository;
import com.example.androidfronted.ui.adapter.ApplicationRecordAdapter;
import com.example.androidfronted.viewmodel.base.NavigationEvent;
import com.example.androidfronted.viewmodel.loan.ApplicationRecordsViewModel;

public class ApplicationRecordsFragment extends com.example.androidfronted.ui.base.BaseDetailFragment {
    private static final String TAG = "ApplicationRecordsFragment";

    private ApplicationRecordsViewModel viewModel;
    private ApplicationRecordAdapter adapter;

    private RecyclerView recyclerView;
    private TextView tvRecordCount;
    private Button btnFilterAll;
    private Button btnFilterPending;
    private Button btnFilterApproved;
    private Button btnFilterRejected;
    private Button btnFilterCancelled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ApplicationRecordsViewModel.class);
        adapter = new ApplicationRecordAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_application_records, container, false);
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

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_application_records);
        tvRecordCount = view.findViewById(R.id.tv_record_count);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterPending = view.findViewById(R.id.btn_filter_pending);
        btnFilterApproved = view.findViewById(R.id.btn_filter_approved);
        btnFilterRejected = view.findViewById(R.id.btn_filter_rejected);
        btnFilterCancelled = view.findViewById(R.id.btn_filter_cancelled);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(application -> {
            navigateToDetail(application);
        });

        adapter.setOnCancelClickListener(application -> {
            showCancelConfirmDialog(application);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && getContext() != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getFilteredApplications().observe(getViewLifecycleOwner(), applications -> {
            if (applications != null) {
                adapter.setApplications(applications);
            }
        });

        viewModel.getRecordCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && getContext() != null) {
                String text = getString(R.string.record_count, count);
                tvRecordCount.setText(text);
            }
        });

        viewModel.getCurrentFilter().observe(getViewLifecycleOwner(), filter -> {
            updateFilterButtons(filter);
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void setupClickListeners() {
        btnFilterAll.setOnClickListener(v -> viewModel.setFilter(ApplicationRecordsViewModel.FILTER_ALL));
        btnFilterPending.setOnClickListener(v -> viewModel.setFilter(ApplicationRecordsViewModel.FILTER_PENDING));
        btnFilterApproved.setOnClickListener(v -> viewModel.setFilter(ApplicationRecordsViewModel.FILTER_APPROVED));
        btnFilterRejected.setOnClickListener(v -> viewModel.setFilter(ApplicationRecordsViewModel.FILTER_REJECTED));
        btnFilterCancelled.setOnClickListener(v -> viewModel.setFilter(ApplicationRecordsViewModel.FILTER_CANCELLED));
    }

    private void loadData() {
        viewModel.loadApplications();
    }

    private void updateFilterButtons(String currentFilter) {
        resetAllFilterButtons();

        Button selectedBtn = null;
        if (ApplicationRecordsViewModel.FILTER_ALL.equals(currentFilter)) {
            selectedBtn = btnFilterAll;
        } else if (ApplicationRecordsViewModel.FILTER_PENDING.equals(currentFilter)) {
            selectedBtn = btnFilterPending;
        } else if (ApplicationRecordsViewModel.FILTER_APPROVED.equals(currentFilter)) {
            selectedBtn = btnFilterApproved;
        } else if (ApplicationRecordsViewModel.FILTER_REJECTED.equals(currentFilter)) {
            selectedBtn = btnFilterRejected;
        } else if (ApplicationRecordsViewModel.FILTER_CANCELLED.equals(currentFilter)) {
            selectedBtn = btnFilterCancelled;
        }

        if (selectedBtn != null) {
            selectedBtn.setSelected(true);
        }
    }

    private void resetAllFilterButtons() {
        btnFilterAll.setSelected(false);
        btnFilterPending.setSelected(false);
        btnFilterApproved.setSelected(false);
        btnFilterRejected.setSelected(false);
        btnFilterCancelled.setSelected(false);
    }

    private void navigateToDetail(ApplicationEntity application) {
        if (getActivity() != null) {
            ApplicationRecordDetailFragment fragment = ApplicationRecordDetailFragment.newInstance(
                    application.getApplicationId(),
                    application.getProductName()
            );
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void showCancelConfirmDialog(ApplicationEntity application) {
        if (getContext() != null) {
            // 创建自定义对话框
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_application_confirm, null);
            builder.setView(dialogView);
            
            // 获取按钮
            Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);
            Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
            
            final android.app.AlertDialog dialog = builder.create();
            
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                // 执行取消申请操作
                viewModel.withdrawApplication(application.getApplicationId(), new LoanApplicationRepository.CallbackResult() {
                    @Override
                    public void onSuccess(String successMessage) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        // 错误信息已在ViewModel中处理
                    }
                });
            });
            
            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });
            
            dialog.show();
        }
    }

    private void handleNavigation(NavigationEvent event) {
        if (event.getNavigationType() == NavigationEvent.NAVIGATE_BACK && getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
