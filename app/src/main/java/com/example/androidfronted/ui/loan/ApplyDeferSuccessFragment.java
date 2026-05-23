package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.androidfronted.R;

public class ApplyDeferSuccessFragment extends Fragment {
    private static final String ARG_ORDER_ID = "order_id";

    private int orderId;

    public static ApplyDeferSuccessFragment newInstance(int orderId) {
        ApplyDeferSuccessFragment fragment = new ApplyDeferSuccessFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply_defer_success, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        Button btnBackToList = view.findViewById(R.id.btnBackToList);

        btnBackToList.setOnClickListener(v -> {
            navigateBackToOrderDetail();
        });
    }

    private void navigateBackToOrderDetail() {
        if (getActivity() != null) {
            androidx.fragment.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }
    }
}
