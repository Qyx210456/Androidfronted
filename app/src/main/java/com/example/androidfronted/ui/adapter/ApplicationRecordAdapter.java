package com.example.androidfronted.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ApplicationRecordAdapter extends RecyclerView.Adapter<ApplicationRecordAdapter.ViewHolder> {

    private List<ApplicationEntity> applications = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private OnCancelClickListener onCancelClickListener;

    public void setApplications(List<ApplicationEntity> applications) {
        this.applications = applications != null ? applications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationEntity application = applications.get(position);
        holder.bind(application);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View statusLine;
        ImageView statusIcon;
        TextView loanName;
        TextView loanAmount;
        TextView applicationTime;
        Button btnCancel;
        Button btnDetail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusLine = itemView.findViewById(R.id.status_line);
            statusIcon = itemView.findViewById(R.id.status_icon);
            loanName = itemView.findViewById(R.id.loan_name);
            loanAmount = itemView.findViewById(R.id.loan_amount);
            applicationTime = itemView.findViewById(R.id.application_time);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }

        void bind(ApplicationEntity application) {
            loanName.setText(application.getProductName());
            
            DecimalFormat df = new DecimalFormat("#,##0.00");
            loanAmount.setText("¥" + df.format(application.getLoanAmount()));
            
            String applyTime = application.getApplyTime();
            if (applyTime != null && !applyTime.isEmpty()) {
                applicationTime.setText(applyTime);
            } else {
                applicationTime.setText("--");
            }

            String status = application.getStatus();
            applyStatusStyle(status);

            boolean isPending = "审核中".equals(status) || "AI拒绝".equals(status);
            btnCancel.setVisibility(isPending ? View.VISIBLE : View.GONE);

            btnCancel.setOnClickListener(v -> {
                if (onCancelClickListener != null) {
                    onCancelClickListener.onCancelClick(application);
                }
            });

            btnDetail.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(application);
                }
            });
        }

        private void applyStatusStyle(String status) {
            int lineColor;
            int iconResId;

            if ("审核中".equals(status) || "AI拒绝".equals(status)) {
                lineColor = Color.parseColor("#FF2D7FF9");
                iconResId = R.drawable.application_status_pending;
            } else if ("已通过".equals(status)) {
                lineColor = Color.parseColor("#FF22BC51");
                iconResId = R.drawable.application_status_approved;
            } else if ("人工拒绝".equals(status)) {
                lineColor = Color.parseColor("#FFF44336");
                iconResId = R.drawable.application_status_rejected;
            } else if ("已取消".equals(status)) {
                lineColor = Color.parseColor("#FFBFBFBF");
                iconResId = R.drawable.application_status_cancelled;
            } else {
                lineColor = Color.parseColor("#FFBFBFBF");
                iconResId = R.drawable.application_status_pending;
            }

            statusLine.setBackgroundColor(lineColor);
            statusIcon.setImageResource(iconResId);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ApplicationEntity application);
    }

    public interface OnCancelClickListener {
        void onCancelClick(ApplicationEntity application);
    }
}
