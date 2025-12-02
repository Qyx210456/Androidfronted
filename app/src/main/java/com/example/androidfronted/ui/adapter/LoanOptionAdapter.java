package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;


/**
 * 贷款方案选项适配器
 * 用于 ProductDetailActivity 中展示多个 LoanOption
 */
public class LoanOptionAdapter extends RecyclerView.Adapter<LoanOptionAdapter.ViewHolder> {

    private final List<LoanProduct.LoanOption> options;
    private LoanProduct.LoanOption selectedOption = null;
    private final OnOptionSelectListener selectListener;
    public interface OnOptionSelectListener {
        void onOptionSelected(LoanProduct.LoanOption option);
    }

    public LoanOptionAdapter(List<LoanProduct.LoanOption> options, OnOptionSelectListener listener) {
        this.options = options;
        this.selectListener = listener;
    }

    public LoanProduct.LoanOption getSelectedOption() {
        return selectedOption;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loan_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoanProduct.LoanOption option = options.get(position);
        boolean isSelected = Objects.equals(selectedOption, option);
        holder.bind(option, isSelected);
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoanAmount, tvInterestRate, tvLoanPeriod, tvRepaidType;
        RadioButton rbSelected;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLoanAmount = itemView.findViewById(R.id.tvLoanAmount);
            tvInterestRate = itemView.findViewById(R.id.tvInterestRate);
            tvLoanPeriod = itemView.findViewById(R.id.tvLoanPeriod);
            tvRepaidType = itemView.findViewById(R.id.tvRepaidType);
            rbSelected = itemView.findViewById(R.id.rbSelected);
        }

        /**
         * 将 LoanOption 数据绑定到 UI 控件
         *
         * @param option 要绑定的贷款方案对象
         */
        void bind(LoanProduct.LoanOption option,boolean isSelected) {
            // 格式化贷款金额：¥50,000
            String amount = String.format("¥%,.0f", option.getLoanAmount());
            tvLoanAmount.setText(amount);

            // 格式化年化利率：4.90%
            String rate = new DecimalFormat("#.##%").format(option.getInterestRate());
            tvInterestRate.setText(rate);

            // 处理贷款总时长（loanPeriod 单位：月）→ 转换为“X年Y个月”
            int totalMonths = option.getLoanPeriod();
            String periodText;
            if (totalMonths % 12 == 0) {
                periodText = (totalMonths / 12) + "年";
            } else {
                int years = totalMonths / 12;
                int months = totalMonths % 12;
                periodText = (years > 0 ? years + "年" : "") + months + "个月";
            }
            tvLoanPeriod.setText(periodText); // 仅显示值，label 在 XML 中

            // 还款方式
            tvRepaidType.setText(option.getRepaidType());

            // 申请按钮点击事件
            rbSelected.setChecked(isSelected);

            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_option_card_selected);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_option_card);
            }

            itemView.setOnClickListener(v -> selectOption(option));
            rbSelected.setOnClickListener(v -> selectOption(option));
        }

        private void selectOption(LoanProduct.LoanOption option) {
            //  更新选中项并刷新 UI
            if (!Objects.equals(selectedOption, option)) {
                LoanProduct.LoanOption old = selectedOption;
                selectedOption = option;

                // 刷新新旧项
                if (old != null) {
                    int oldPos = options.indexOf(old);
                    if (oldPos >= 0) notifyItemChanged(oldPos);
                }
                notifyItemChanged(getAdapterPosition());

                if (selectListener != null) {
                    selectListener.onOptionSelected(option);
                }
            }
        }

    }
}