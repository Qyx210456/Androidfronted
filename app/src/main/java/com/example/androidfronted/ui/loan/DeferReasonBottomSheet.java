package com.example.androidfronted.ui.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.util.FloatingBallManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeferReasonBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_REASONS = "reasons";
    private static final String ARG_SELECTED_POSITION = "selected_position";

    private String[] reasons;
    private int selectedPosition = -1;
    private OnReasonSelectedListener listener;

    public interface OnReasonSelectedListener {
        void onReasonSelected(String reason, int position);
    }

    public static DeferReasonBottomSheet newInstance(String[] reasons, int selectedPosition) {
        DeferReasonBottomSheet fragment = new DeferReasonBottomSheet();
        Bundle args = new Bundle();
        args.putStringArray(ARG_REASONS, reasons);
        args.putInt(ARG_SELECTED_POSITION, selectedPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnReasonSelectedListener(OnReasonSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reasons = getArguments().getStringArray(ARG_REASONS);
            selectedPosition = getArguments().getInt(ARG_SELECTED_POSITION, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_defer_reason, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        FloatingBallManager.getInstance(requireActivity().getApplication()).temporarilyHideBall();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ReasonAdapter());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingBallManager.getInstance(requireActivity().getApplication()).restoreBall();
    }

    private class ReasonAdapter extends RecyclerView.Adapter<ReasonViewHolder> {

        @NonNull
        @Override
        public ReasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bottom_sheet_reason, parent, false);
            return new ReasonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReasonViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return reasons != null ? reasons.length : 0;
        }
    }

    private class ReasonViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvReason;
        private final ImageView ivCheck;
        private final View divider;

        public ReasonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReason = itemView.findViewById(R.id.tv_reason);
            ivCheck = itemView.findViewById(R.id.iv_check);
            divider = itemView.findViewById(R.id.divider);
        }

        public void bind(int position) {
            tvReason.setText(reasons[position]);
            
            boolean isSelected = (position == selectedPosition);
            ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            tvReason.setTextColor(getResources().getColor(
                isSelected ? R.color.defer_primary_blue : R.color.defer_dark_blue
            ));
            
            divider.setVisibility(position == reasons.length - 1 ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(v -> {
                selectedPosition = position;
                
                if (listener != null) {
                    listener.onReasonSelected(reasons[position], position);
                }
                
                dismiss();
            });
        }
    }
}
