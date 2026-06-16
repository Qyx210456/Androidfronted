package com.example.androidfronted.ui.personalinformationinfo;

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
import java.util.List;

public class UploadTypeBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TYPES = "types";
    private static final String ARG_SELECTED_POSITION = "selected_position";

    private List<String> types;
    private int selectedPosition = -1;
    private OnTypeSelectedListener listener;

    public interface OnTypeSelectedListener {
        void onTypeSelected(String type, int position);
    }

    public static UploadTypeBottomSheet newInstance(List<String> types, int selectedPosition) {
        UploadTypeBottomSheet fragment = new UploadTypeBottomSheet();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_TYPES, new java.util.ArrayList<>(types));
        args.putInt(ARG_SELECTED_POSITION, selectedPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnTypeSelectedListener(OnTypeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            types = getArguments().getStringArrayList(ARG_TYPES);
            selectedPosition = getArguments().getInt(ARG_SELECTED_POSITION, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_upload_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        FloatingBallManager.getInstance(requireActivity().getApplication()).temporarilyHideBall();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new TypeAdapter());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingBallManager.getInstance(requireActivity().getApplication()).restoreBall();
    }

    private class TypeAdapter extends RecyclerView.Adapter<TypeViewHolder> {

        @NonNull
        @Override
        public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bottom_sheet_upload_type, parent, false);
            return new TypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return types != null ? types.size() : 0;
        }
    }

    private class TypeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvType;
        private final ImageView ivCheck;
        private final View divider;

        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_type);
            ivCheck = itemView.findViewById(R.id.iv_check);
            divider = itemView.findViewById(R.id.divider);
        }

        public void bind(int position) {
            tvType.setText(types.get(position));
            
            boolean isSelected = (position == selectedPosition);
            ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            tvType.setTextColor(getResources().getColor(
                isSelected ? R.color.defer_primary_blue : R.color.text_secondary
            ));
            
            divider.setVisibility(position == types.size() - 1 ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(v -> {
                selectedPosition = position;
                
                if (listener != null) {
                    listener.onTypeSelected(types.get(position), position);
                }
                
                dismiss();
            });
        }
    }
}
