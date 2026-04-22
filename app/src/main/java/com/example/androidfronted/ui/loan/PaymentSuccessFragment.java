package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.androidfronted.R;

public class PaymentSuccessFragment extends Fragment {
    private static final String ARG_ORDER_ID = "order_id";
    private static final String ARG_TERM = "term";
    public static final String PAYMENT_SUCCESS_REQUEST_KEY = "payment_success_request";
    public static final String PAYMENT_SUCCESS_ORDER_ID = "payment_success_order_id";

    private int orderId;
    private int term;

    public static PaymentSuccessFragment newInstance(int orderId, int term) {
        PaymentSuccessFragment fragment = new PaymentSuccessFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        args.putInt(ARG_TERM, term);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID, 0);
            term = getArguments().getInt(ARG_TERM, 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repayment_success, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        TextView tvSuccessDesc = view.findViewById(R.id.tv_success_desc);
        Button btnBackToList = view.findViewById(R.id.btnBackToList);

        tvSuccessDesc.setText(getString(R.string.payment_success_term_format, term));

        btnBackToList.setOnClickListener(v -> {
            navigateBackToOrderDetail();
        });
    }

    private void navigateBackToOrderDetail() {
        Bundle result = new Bundle();
        result.putInt(PAYMENT_SUCCESS_ORDER_ID, orderId);
        getParentFragmentManager().setFragmentResult(PAYMENT_SUCCESS_REQUEST_KEY, result);
        
        if (getActivity() != null) {
            androidx.fragment.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }
    }
}
