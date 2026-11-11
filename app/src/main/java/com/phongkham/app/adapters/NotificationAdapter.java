package com.phongkham.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private List<NotificationItem> list;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item);
    }

    public NotificationAdapter(Context context, List<NotificationItem> list, OnNotificationClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);
        holder.tvNotifTitle.setText(item.getTitle());
        holder.tvNotifContent.setText(item.getContent());
        holder.tvNotifTime.setText(item.getCreatedAt());

        if (NotificationItem.TYPE_APPOINTMENT.equals(item.getType())) {
            holder.tvNotifIcon.setText("📅");
        } else if (NotificationItem.TYPE_REMINDER.equals(item.getType())) {
            holder.tvNotifIcon.setText("⏰");
        } else if (NotificationItem.TYPE_RESULT.equals(item.getType())) {
            holder.tvNotifIcon.setText("📋");
        } else {
            holder.tvNotifIcon.setText("🔔");
        }

        if (!item.isRead()) {
            holder.layoutContainer.setBackgroundColor(Color.parseColor("#EBF5FB"));
        } else {
            holder.layoutContainer.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutContainer;
        TextView tvNotifIcon, tvNotifTitle, tvNotifContent, tvNotifTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContainer = itemView.findViewById(R.id.layoutNotificationContainer);
            tvNotifIcon = itemView.findViewById(R.id.tvNotifIcon);
            tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvNotifContent = itemView.findViewById(R.id.tvNotifContent);
            tvNotifTime = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}
