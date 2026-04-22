package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.loan.LoanManageViewModel;

public class LoanFragment extends Fragment {

    private LoanManageViewModel viewModel;
    private TextView tvAmount;
    private TextView tvPrincipal;
    private TextView tvInterest;
    private TextView tvTotal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(LoanManageViewModel.class);
        
        initViews(view);
        observeData();
        
        view.findViewById(R.id.item_application).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.androidfronted.ui.loan.ApplicationRecordsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        view.findViewById(R.id.item_loans).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.androidfronted.ui.loan.LoanOrdersFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        view.findViewById(R.id.item_repayment_history).setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "还款记录功能待实现", android.widget.Toast.LENGTH_SHORT).show();
        });
    }
    
    private void initViews(View view) {
        tvAmount = view.findViewById(R.id.tv_amount);
        tvPrincipal = view.findViewById(R.id.tv_principal);
        tvInterest = view.findViewById(R.id.tv_interest);
        tvTotal = view.findViewById(R.id.tv_total);
    }
    
    private void observeData() {
        viewModel.getTotalPrincipal().observe(getViewLifecycleOwner(), principal -> {
            if (principal != null) {
                tvPrincipal.setText(viewModel.formatAmount(principal));
            }
        });
        
        viewModel.getTotalInterest().observe(getViewLifecycleOwner(), interest -> {
            if (interest != null) {
                tvInterest.setText(viewModel.formatAmount(interest));
            }
        });
        
        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                tvTotal.setText(viewModel.formatAmount(total));
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadUnpaidStats();
        if (getActivity() instanceof com.example.androidfronted.ui.MainActivity) {
            ((com.example.androidfronted.ui.MainActivity) getActivity()).setBottomNavigationVisible(true);
        }
    }
}
