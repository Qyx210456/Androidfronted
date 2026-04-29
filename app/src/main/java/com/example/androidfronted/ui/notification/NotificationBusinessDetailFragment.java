package com.example.androidfronted.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.ui.loan.ApplicationRecordDetailFragment;
import com.example.androidfronted.ui.loan.LoanOrderDetailFragment;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.notification.NotificationDetailViewModel;

public class NotificationBusinessDetailFragment extends Fragment {
    
    private static final String ARG_NOTIFICATION_ID = "notification_id";
    private static final String ARG_BUSINESS_ID = "business_id";
    private static final String ARG_BUSINESS_TYPE = "business_type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";
    private static final String ARG_CREATED_AT = "created_at";
    
    private TextView tvTitle;
    private TextView tvTime;
    private TextView tvContent;
    private Button btnAction;
    
    private NotificationDetailViewModel viewModel;
    
    private int notificationId;
    private int businessId;
    private String businessType;
    private String title;
    private String content;
    private String createdAt;
    
    public static NotificationBusinessDetailFragment newInstance(
            int notificationId, int businessId, String businessType, 
            String title, String content, String createdAt) {
        NotificationBusinessDetailFragment fragment = new NotificationBusinessDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NOTIFICATION_ID, notificationId);
        args.putInt(ARG_BUSINESS_ID, businessId);
        args.putString(ARG_BUSINESS_TYPE, businessType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putString(ARG_CREATED_AT, createdAt);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("NotificationBusinessDetailFragment", "onCreate called");
        
        if (getArguments() != null) {
            notificationId = getArguments().getInt(ARG_NOTIFICATION_ID);
            businessId = getArguments().getInt(ARG_BUSINESS_ID);
            businessType = getArguments().getString(ARG_BUSINESS_TYPE);
            title = getArguments().getString(ARG_TITLE);
            content = getArguments().getString(ARG_CONTENT);
            createdAt = getArguments().getString(ARG_CREATED_AT);
            
            android.util.Log.d("NotificationBusinessDetailFragment", "notificationId: " + notificationId);
            android.util.Log.d("NotificationBusinessDetailFragment", "businessId: " + businessId);
            android.util.Log.d("NotificationBusinessDetailFragment", "businessType: " + businessType);
        }
        
        viewModel = new ViewModelProvider(this, new ViewModelFactory(requireActivity().getApplication()))
                .get(NotificationDetailViewModel.class);
        
        android.util.Log.d("NotificationBusinessDetailFragment", "ViewModel created");
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d("NotificationBusinessDetailFragment", "onCreateView called");
        return inflater.inflate(R.layout.profile_menu_notification__business_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("NotificationBusinessDetailFragment", "onViewCreated called");
        initViews(view);
        setupObservers();
        setupClickListener();
        loadData();
        android.util.Log.d("NotificationBusinessDetailFragment", "onViewCreated completed");
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvTime = view.findViewById(R.id.tv_time);
        tvContent = view.findViewById(R.id.tv_content);
        btnAction = view.findViewById(R.id.btn_action);
        
        view.findViewById(R.id.apply_btn_back).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }
    
    private void setupObservers() {
        viewModel.getDisplayTitle().observe(getViewLifecycleOwner(), displayTitle -> {
            if (displayTitle != null) {
                tvTitle.setText(displayTitle);
            }
        });
        
        viewModel.getFormattedContent().observe(getViewLifecycleOwner(), formattedContent -> {
            if (formattedContent != null) {
                tvContent.setText(formattedContent);
            }
        });
        
        viewModel.getButtonText().observe(getViewLifecycleOwner(), buttonText -> {
            if (buttonText != null) {
                btnAction.setText(buttonText);
            }
        });
        
        viewModel.getFormattedTime().observe(getViewLifecycleOwner(), time -> {
            if (time != null) {
                tvTime.setText(time);
            }
        });
    }
    
    private void setupClickListener() {
        btnAction.setOnClickListener(v -> navigateToDetail());
    }
    
    private void loadData() {
        try {
            android.util.Log.d("NotificationBusinessDetailFragment", "loadData called");
            
            NotificationEntity notification = new NotificationEntity(
                notificationId, 
                0, 
                businessId, 
                businessType != null ? businessType : "", 
                title != null ? title : "", 
                content != null ? content : "", 
                false, 
                createdAt != null ? createdAt : "", 
                null
            );
            
            android.util.Log.d("NotificationBusinessDetailFragment", "NotificationEntity created");
            
            viewModel.setNotification(requireContext(), notification);
            
            android.util.Log.d("NotificationBusinessDetailFragment", "setNotification called");
            
            viewModel.markAsRead(notificationId);
            
            android.util.Log.d("NotificationBusinessDetailFragment", "markAsRead called");
        } catch (Exception e) {
            android.util.Log.e("NotificationBusinessDetailFragment", "Error in loadData", e);
        }
    }
    
    private void navigateToDetail() {
        if ("LOAN_APPLICATION_STATUS".equals(businessType)) {
            navigateToApplicationDetail(businessId);
        } else if ("REPAYMENT".equals(businessType)) {
            navigateToOrderDetail(businessId);
        }
    }
    
    private void navigateToApplicationDetail(int applicationId) {
        ApplicationRecordDetailFragment fragment = 
            ApplicationRecordDetailFragment.newInstance(applicationId, null);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }
    
    private void navigateToOrderDetail(int orderId) {
        LoanOrderDetailFragment fragment = LoanOrderDetailFragment.newInstance(orderId);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }
}
