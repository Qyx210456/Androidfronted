package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NOTIFICATION = 0;
    private static final int TYPE_FOOTER = 1;

    private final List<NotificationEntity> notifications = new ArrayList<>();
    private final Context context;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationEntity notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setNotifications(List<NotificationEntity> notificationList) {
        android.util.Log.d("NotificationAdapter", "setNotifications called, notifications size: " + (notificationList != null ? notificationList.size() : 0));
        
        notifications.clear();
        if (notificationList != null) {
            notifications.addAll(notificationList);
        }
        
        android.util.Log.d("NotificationAdapter", "Total items: " + notifications.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == notifications.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_NOTIFICATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_profile_menu_notification_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_profile_menu_notification_card, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NotificationViewHolder) {
            NotificationEntity notification = notifications.get(position);
            ((NotificationViewHolder) holder).bind(notification, listener);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.isEmpty() ? 0 : notifications.size() + 1;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNotificationIcon;
        ImageView ivUnreadDot;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;

        NotificationViewHolder(View itemView) {
            super(itemView);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(NotificationEntity notification, OnNotificationClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvContent.setText(notification.getContent());
            tvTime.setText(DateUtils.formatDateTime(notification.getCreatedAt()));
            
            if (notification.isReadFlag()) {
                ivUnreadDot.setVisibility(View.GONE);
            } else {
                ivUnreadDot.setVisibility(View.VISIBLE);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                    ivUnreadDot.setVisibility(View.GONE);
                }
            });
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
