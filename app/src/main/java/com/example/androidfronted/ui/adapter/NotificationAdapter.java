package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.util.DateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NOTIFICATION = 0;
    private static final int TYPE_FOOTER = 1;

    private final List<NotificationEntity> notifications = new ArrayList<>();
    private final Context context;
    private final OnNotificationClickListener listener;
    
    private boolean isMultiSelectMode = false;
    private final Set<Integer> selectedIds = new HashSet<>();
    private OnMultiSelectListener multiSelectListener;
    private int openedMenuPosition = -1;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationEntity notification);
    }

    public interface OnMultiSelectListener {
        void onEnterMultiSelectMode();
        void onDeleteSingle(NotificationEntity notification);
        void onSelectedCountChanged(int count);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMultiSelectListener(OnMultiSelectListener listener) {
        this.multiSelectListener = listener;
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

    public void setMultiSelectMode(boolean mode) {
        if (this.isMultiSelectMode != mode) {
            this.isMultiSelectMode = mode;
            if (!mode) {
                selectedIds.clear();
                openedMenuPosition = -1;
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public Set<Integer> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (multiSelectListener != null) {
            multiSelectListener.onSelectedCountChanged(0);
        }
    }

    public void removeNotifications(List<Integer> ids) {
        for (int id : ids) {
            for (int i = 0; i < notifications.size(); i++) {
                if (notifications.get(i).getId() == id) {
                    notifications.remove(i);
                    break;
                }
            }
        }
        selectedIds.removeAll(ids);
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
            ((NotificationViewHolder) holder).bind(notification, listener, position);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.isEmpty() ? 0 : notifications.size() + 1;
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNotificationIcon;
        ImageView ivUnreadDot;
        ImageView ivMoreOptions;
        ImageView ivSelectCheckbox;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        TextView tvViewDetail;
        LinearLayout llMoreMenu;
        TextView tvMultiSelect;
        TextView tvDeleteSingle;

        NotificationViewHolder(View itemView) {
            super(itemView);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
            ivSelectCheckbox = itemView.findViewById(R.id.ivSelectCheckbox);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvViewDetail = itemView.findViewById(R.id.tvViewDetail);
            llMoreMenu = itemView.findViewById(R.id.llMoreMenu);
            tvMultiSelect = itemView.findViewById(R.id.tvMultiSelect);
            tvDeleteSingle = itemView.findViewById(R.id.tvDeleteSingle);
        }

        void bind(NotificationEntity notification, OnNotificationClickListener listener, int position) {
            tvTitle.setText(notification.getTitle());
            tvContent.setText(notification.getContent());
            tvTime.setText(DateUtils.formatDateTime(notification.getCreatedAt()));
            
            if (notification.isReadFlag()) {
                ivUnreadDot.setVisibility(View.GONE);
            } else {
                ivUnreadDot.setVisibility(View.VISIBLE);
            }

            if (isMultiSelectMode) {
                ivSelectCheckbox.setVisibility(View.VISIBLE);
                ivMoreOptions.setVisibility(View.GONE);
                llMoreMenu.setVisibility(View.GONE);
                tvViewDetail.setVisibility(View.GONE);
                
                boolean isSelected = selectedIds.contains(notification.getId());
                ivSelectCheckbox.setSelected(isSelected);
                
                ivSelectCheckbox.setOnClickListener(v -> {
                    if (selectedIds.contains(notification.getId())) {
                        selectedIds.remove(notification.getId());
                        ivSelectCheckbox.setSelected(false);
                    } else {
                        selectedIds.add(notification.getId());
                        ivSelectCheckbox.setSelected(true);
                    }
                    if (multiSelectListener != null) {
                        multiSelectListener.onSelectedCountChanged(selectedIds.size());
                    }
                });
                
                itemView.setOnClickListener(v -> {
                    if (selectedIds.contains(notification.getId())) {
                        selectedIds.remove(notification.getId());
                        ivSelectCheckbox.setSelected(false);
                    } else {
                        selectedIds.add(notification.getId());
                        ivSelectCheckbox.setSelected(true);
                    }
                    if (multiSelectListener != null) {
                        multiSelectListener.onSelectedCountChanged(selectedIds.size());
                    }
                });
            } else {
                ivSelectCheckbox.setVisibility(View.GONE);
                ivMoreOptions.setVisibility(View.VISIBLE);
                tvViewDetail.setVisibility(View.VISIBLE);
                
                if (openedMenuPosition == position) {
                    llMoreMenu.setVisibility(View.VISIBLE);
                } else {
                    llMoreMenu.setVisibility(View.GONE);
                }
                
                ivMoreOptions.setOnClickListener(v -> {
                    if (llMoreMenu.getVisibility() == View.VISIBLE) {
                        llMoreMenu.setVisibility(View.GONE);
                        openedMenuPosition = -1;
                    } else {
                        int previousPosition = openedMenuPosition;
                        openedMenuPosition = position;
                        llMoreMenu.setVisibility(View.VISIBLE);
                        if (previousPosition != -1 && previousPosition != position) {
                            notifyItemChanged(previousPosition);
                        }
                    }
                });
                
                tvMultiSelect.setOnClickListener(v -> {
                    llMoreMenu.setVisibility(View.GONE);
                    openedMenuPosition = -1;
                    if (multiSelectListener != null) {
                        multiSelectListener.onEnterMultiSelectMode();
                    }
                });
                
                tvDeleteSingle.setOnClickListener(v -> {
                    llMoreMenu.setVisibility(View.GONE);
                    openedMenuPosition = -1;
                    if (multiSelectListener != null) {
                        multiSelectListener.onDeleteSingle(notification);
                    }
                });
                
                itemView.setOnClickListener(v -> {
                    if (llMoreMenu.getVisibility() == View.VISIBLE) {
                        llMoreMenu.setVisibility(View.GONE);
                        openedMenuPosition = -1;
                    } else {
                        if (listener != null) {
                            listener.onNotificationClick(notification);
                            ivUnreadDot.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
