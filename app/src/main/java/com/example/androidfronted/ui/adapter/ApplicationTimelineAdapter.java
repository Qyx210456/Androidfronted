package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.viewmodel.loan.ApplicationDetailViewModel;
import java.util.List;

public class ApplicationTimelineAdapter extends RecyclerView.Adapter<ApplicationTimelineAdapter.TimelineViewHolder> {
    private List<ApplicationDetailViewModel.TimelineStep> timelineSteps;

    public ApplicationTimelineAdapter(List<ApplicationDetailViewModel.TimelineStep> timelineSteps) {
        this.timelineSteps = timelineSteps;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application_record_detail_timeline_step, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        ApplicationDetailViewModel.TimelineStep step = timelineSteps.get(position);
        holder.bind(step, position == getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return timelineSteps != null ? timelineSteps.size() : 0;
    }

    public void updateData(List<ApplicationDetailViewModel.TimelineStep> newSteps) {
        this.timelineSteps = newSteps;
        notifyDataSetChanged();
    }

    class TimelineViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivStatusIcon;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvTime;
        private final View viewLine;

        TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTime = itemView.findViewById(R.id.tv_time);
            viewLine = itemView.findViewById(R.id.view_line);
        }

        void bind(ApplicationDetailViewModel.TimelineStep step, boolean isLastItem) {
            tvTitle.setText(step.getTitle());
            tvDescription.setText(step.getDescription());
            tvTime.setText(step.getTime());

            // 设置图标
            switch (step.getStatus()) {
                case COMPLETED:
                    ivStatusIcon.setImageResource(R.drawable.ic_application_record_detail_progress_true);
                    break;
                case ONGOING:
                    ivStatusIcon.setImageResource(R.drawable.ic_application_record_detail_progress_ongoing);
                    break;
                case FUTURE:
                    ivStatusIcon.setImageResource(R.drawable.ic_application_record_detail_progress_false);
                    break;
            }

            // 设置线条颜色
            if (isLastItem) {
                viewLine.setVisibility(View.GONE);
            } else {
                viewLine.setVisibility(View.VISIBLE);
                ApplicationDetailViewModel.TimelineStep nextStep = timelineSteps.get(getAdapterPosition() + 1);
                if (step.getStatus() == ApplicationDetailViewModel.TimelineStatus.COMPLETED && 
                    (nextStep.getStatus() == ApplicationDetailViewModel.TimelineStatus.COMPLETED || 
                     nextStep.getStatus() == ApplicationDetailViewModel.TimelineStatus.ONGOING)) {
                    viewLine.setBackgroundColor(itemView.getResources().getColor(R.color.application_status_pending));
                } else {
                    viewLine.setBackgroundColor(itemView.getResources().getColor(R.color.application_status_cancelled));
                }
            }
        }
    }
}