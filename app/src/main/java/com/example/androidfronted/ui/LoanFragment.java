package com.example.androidfronted.ui;

import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "LoanFragment";
    
    private LoanManageViewModel viewModel;
    private TextView tvAmount;
    private TextView tvPrincipal;
    private TextView tvInterest;
    private TextView tvTotal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_loan_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(LoanManageViewModel.class);
        
        initViews(view);
        observeData();
        
        view.findViewById(R.id.card_my_application).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.androidfronted.ui.loan.ApplicationRecordsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        view.findViewById(R.id.card_my_orders).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.androidfronted.ui.loan.LoanOrdersFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        view.findViewById(R.id.card_repayment_record).setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "还款记录功能待实现", android.widget.Toast.LENGTH_SHORT).show();
        });
    }
    
    private void initViews(View view) {
        Log.d(TAG, "initViews");
        tvAmount = view.findViewById(R.id.tv_amount);
        tvPrincipal = view.findViewById(R.id.tv_principal);
        tvInterest = view.findViewById(R.id.tv_interest);
        tvTotal = view.findViewById(R.id.tv_total);
    }
    
    private void observeData() {
        Log.d(TAG, "observeData: setting up LiveData observers");
        
        viewModel.getTotalPrincipal().observe(getViewLifecycleOwner(), principal -> {
            Log.d(TAG, "totalPrincipal changed: " + principal);
            if (principal != null) {
                String formatted = viewModel.formatAmount(principal);
                Log.d(TAG, "Setting tv_principal to: " + formatted);
                tvPrincipal.setText(formatted);
            }
        });
        
        viewModel.getTotalInterest().observe(getViewLifecycleOwner(), interest -> {
            Log.d(TAG, "totalInterest changed: " + interest);
            if (interest != null) {
                String formatted = viewModel.formatAmount(interest);
                Log.d(TAG, "Setting tv_interest to: " + formatted);
                tvInterest.setText(formatted);
            }
        });
        
        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            Log.d(TAG, "totalAmount changed: " + total);
            if (total != null) {
                String formatted = viewModel.formatAmount(total);
                Log.d(TAG, "Setting tv_total to: " + formatted);
                tvTotal.setText(formatted);
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: calling loadUnpaidStats");
        viewModel.loadUnpaidStats();
        if (getActivity() instanceof com.example.androidfronted.ui.MainActivity) {
            ((com.example.androidfronted.ui.MainActivity) getActivity()).setBottomNavigationVisible(true);
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: hidden=" + hidden);
        if (!hidden) {
            Log.d(TAG, "Fragment shown, calling loadUnpaidStats");
            viewModel.loadUnpaidStats();
        }
    }
}
