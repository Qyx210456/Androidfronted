package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 还款计划列表适配器
 */
public class RepaymentPlanAdapter extends RecyclerView.Adapter<RepaymentPlanAdapter.ViewHolder> {
    private List<RepaymentPlanEntity> planList;
    private final Context context;

    public RepaymentPlanAdapter(Context context) {
        this.context = context;
        this.planList = new ArrayList<>();
    }

    public void setPlanList(List<RepaymentPlanEntity> planList) {
        this.planList = planList != null ? planList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_repayment_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepaymentPlanEntity plan = planList.get(position);
        
        holder.tvPeriod.setText("第" + plan.getTerm() + "期");
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        holder.tvAmount.setText("¥ " + df.format(plan.getTotal()));
        holder.tvPrincipalDue.setText(df.format(plan.getPrincipal()));
        holder.tvInterestDue.setText(df.format(plan.getInterest()));
        
        double principalRemaining = calculateRemainingPrincipal(plan.getTerm());
        double interestRemaining = calculateRemainingInterest(plan.getTerm());
        holder.tvPrincipalRemaining.setText(formatAmount(principalRemaining));
        holder.tvInterestRemaining.setText(formatAmount(interestRemaining));
        
        bindStatusStyle(holder, plan.getStatus());
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    /**
     * 根据状态设置样式
     */
    private void bindStatusStyle(ViewHolder holder, String status) {
        if (status == null) {
            status = "未还";
        }
        
        switch (status) {
            case "已还":
                holder.cardContainer.setBackgroundResource(R.drawable.bg_repayment_plan_item_card_repaid);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_repayment_plan_corner_status_repaid);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_repaid));
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_repaid));
                break;
            case "逾期":
                holder.cardContainer.setBackgroundResource(R.drawable.bg_repayment_plan_item_card_overdue);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_repayment_plan_corner_status_overdue);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_overdue));
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_overdue));
                break;
            case "未还":
            default:
                holder.cardContainer.setBackgroundResource(R.drawable.bg_repayment_plan_item_card_unpaid);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_repayment_plan_corner_status_unpaid);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_unpaid));
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_unpaid));
                break;
        }
        holder.tvStatus.setText(status);
    }

    /**
     * 计算剩余本金（期数大于该期的principal之和）
     */
    private double calculateRemainingPrincipal(int currentTerm) {
        double total = 0;
        for (RepaymentPlanEntity plan : planList) {
            if (plan.getTerm() > currentTerm) {
                total += plan.getPrincipal();
            }
        }
        return total;
    }

    /**
     * 计算剩余利息（期数大于该期的interest之和）
     */
    private double calculateRemainingInterest(int currentTerm) {
        double total = 0;
        for (RepaymentPlanEntity plan : planList) {
            if (plan.getTerm() > currentTerm) {
                total += plan.getInterest();
            }
        }
        return total;
    }

    /**
     * 格式化金额显示（剩余金额，以元为单位）
     */
    private String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardContainer;
        TextView tvPeriod;
        TextView tvStatus;
        TextView tvAmount;
        TextView tvRepayDate;
        TextView tvPrincipalDue;
        TextView tvInterestDue;
        TextView tvPrincipalRemaining;
        TextView tvInterestRemaining;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvRepayDate = itemView.findViewById(R.id.tv_repay_date);
            tvPrincipalDue = itemView.findViewById(R.id.tv_principal_due);
            tvInterestDue = itemView.findViewById(R.id.tv_interest_due);
            tvPrincipalRemaining = itemView.findViewById(R.id.tv_principal_remaining);
            tvInterestRemaining = itemView.findViewById(R.id.tv_interest_remaining);
        }
    }
}
