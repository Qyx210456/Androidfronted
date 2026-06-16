package com.example.androidfronted.ui.loan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidfronted.R;
import com.example.androidfronted.util.FloatingBallManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class TermPickerBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TERMS = "terms";
    private static final String ARG_SELECTED_TERM = "selected_term";

    private List<Integer> terms;
    private int selectedTerm;
    private OnTermSelectedListener listener;

    public interface OnTermSelectedListener {
        void onTermSelected(int term);
    }

    public static TermPickerBottomSheet newInstance(List<Integer> terms, int selectedTerm) {
        TermPickerBottomSheet fragment = new TermPickerBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TERMS, new java.util.ArrayList<>(terms));
        args.putInt(ARG_SELECTED_TERM, selectedTerm);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnTermSelectedListener(OnTermSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            terms = (List<Integer>) getArguments().getSerializable(ARG_TERMS);
            selectedTerm = getArguments().getInt(ARG_SELECTED_TERM, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_term_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        FloatingBallManager.getInstance(requireActivity().getApplication()).temporarilyHideBall();

        com.example.androidfronted.widget.TermNumberPicker picker = view.findViewById(R.id.numberPicker);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (terms != null && !terms.isEmpty()) {
            picker.setMinValue(0);
            picker.setMaxValue(terms.size() - 1);
            picker.setDisplayedValues(terms.stream().map(t -> t + "期").toArray(String[]::new));

            if (selectedTerm > 0 && terms.contains(selectedTerm)) {
                picker.setValue(terms.indexOf(selectedTerm));
            }
        }

        btnConfirm.setOnClickListener(v -> {
            if (listener != null && terms != null) {
                int selected = terms.get(picker.getValue());
                listener.onTermSelected(selected);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingBallManager.getInstance(requireActivity().getApplication()).restoreBall();
    }
}
