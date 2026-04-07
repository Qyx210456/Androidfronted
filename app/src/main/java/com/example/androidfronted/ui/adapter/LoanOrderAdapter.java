package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LoanOrderAdapter extends RecyclerView.Adapter<LoanOrderAdapter.LoanOrderViewHolder> {
    private List<LoanOrderEntity> orderList;
    private final Context context;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(LoanOrderEntity order);
    }

    public LoanOrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrderList(List<LoanOrderEntity> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LoanOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_loan_order, parent, false);
        return new LoanOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanOrderViewHolder holder, int position) {
        LoanOrderEntity order = orderList.get(position);
        
        // 生成订单编号
        String orderNumber = generateOrderNumber(order);
        holder.tvOrderNumberValue.setText(orderNumber);
        
        // 设置贷款金额
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedAmount = "¥" + decimalFormat.format(order.getLoanAmount());
        holder.tvLoanAmountValue.setText(formattedAmount);
        
        // 根据订单状态设置贷款金额颜色
        String status = order.getStatus();
        switch (status) {
            case "正常":
                holder.tvLoanAmountValue.setTextColor(context.getResources().getColor(R.color.order_status_normal_text));
                break;
            case "已逾期":
                holder.tvLoanAmountValue.setTextColor(context.getResources().getColor(R.color.order_status_overdue_text));
                break;
            case "已完成":
                holder.tvLoanAmountValue.setTextColor(context.getResources().getColor(R.color.order_status_completed_text));
                break;
            default:
                holder.tvLoanAmountValue.setTextColor(context.getResources().getColor(R.color.order_status_normal_text));
        }
        
        // 设置订单生成时间
        holder.tvOrderTimeValue.setText(order.getStartTime());
        
        // 设置订单状态
        setOrderStatus(holder, order.getStatus());
        
        // 设置点击事件
        holder.btnViewDetail.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    private String generateOrderNumber(LoanOrderEntity order) {
        // 从startTime中提取日期和小时，格式为YYYYMMDDHH
        String startTime = order.getStartTime();
        String dateTimePart = startTime.replaceAll("[^0-9]", "").substring(0, 10);
        // 组合订单编号：LN + 日期时间 + 订单ID
        return "LN" + dateTimePart + order.getId();
    }

    private void setOrderStatus(LoanOrderViewHolder holder, String status) {
        switch (status) {
            case "正常":
                holder.tvOrderStatus.setText("正常");
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_loan_order_corner_status_normal);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_normal_text));
                break;
            case "已逾期":
                holder.tvOrderStatus.setText("已逾期");
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_loan_order_corner_status_overdue);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_overdue_text));
                break;
            case "已完成":
                holder.tvOrderStatus.setText("已完成");
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_loan_order_corner_status_completed);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_completed_text));
                break;
            default:
                holder.tvOrderStatus.setText(status);
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_loan_order_corner_status_normal);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_normal_text));
        }
    }

    static class LoanOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumberLabel;
        TextView tvOrderNumberValue;
        TextView tvOrderStatus;
        TextView tvLoanAmountLabel;
        TextView tvLoanAmountValue;
        TextView tvOrderTimeLabel;
        TextView tvOrderTimeValue;
        Button btnViewDetail;

        LoanOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumberLabel = itemView.findViewById(R.id.tv_order_number_label);
            tvOrderNumberValue = itemView.findViewById(R.id.tv_order_number_value);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvLoanAmountLabel = itemView.findViewById(R.id.tv_loan_amount_label);
            tvLoanAmountValue = itemView.findViewById(R.id.tv_loan_amount_value);
            tvOrderTimeLabel = itemView.findViewById(R.id.tv_order_time_label);
            tvOrderTimeValue = itemView.findViewById(R.id.tv_order_time_value);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
        }
    }
}
