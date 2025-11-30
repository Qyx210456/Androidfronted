package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 贷款方案选项适配器
 * 用于 ProductDetailActivity 中展示多个 LoanOption
 */
public class LoanOptionAdapter extends RecyclerView.Adapter<LoanOptionAdapter.ViewHolder> {

    private final List<LoanProduct.LoanOption> options;
    private final OnApplyClickListener listener;

    public LoanOptionAdapter(List<LoanProduct.LoanOption> options, OnApplyClickListener listener) {
        this.options = options;
        this.listener = listener;
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
        holder.bind(options.get(position));
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoanAmount, tvInterestRate, tvLoanPeriod, tvRepaidType;
        Button btnApply;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLoanAmount = itemView.findViewById(R.id.tvLoanAmount);
            tvInterestRate = itemView.findViewById(R.id.tvInterestRate);
            tvLoanPeriod = itemView.findViewById(R.id.tvLoanPeriod);
            tvRepaidType = itemView.findViewById(R.id.tvRepaidType);
            btnApply = itemView.findViewById(R.id.btnApply);
        }

        /**
         * 将 LoanOption 数据绑定到 UI 控件
         *
         * @param option 要绑定的贷款方案对象
         */
        void bind(LoanProduct.LoanOption option) {
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
            btnApply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApplyClick(option);
                }
            });
        }
    }

    public interface OnApplyClickListener {
        void onApplyClick(LoanProduct.LoanOption option);
    }
}