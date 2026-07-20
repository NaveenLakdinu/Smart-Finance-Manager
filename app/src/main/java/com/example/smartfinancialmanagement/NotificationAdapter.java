package com.example.smartfinancialmanagement;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private List<NotificationModel> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setNotifications(List<NotificationModel> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notif = notifications.get(position);
        
        holder.txtTitle.setText(notif.getTitle());
        holder.txtMessage.setText(notif.getMessage());
        
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notif.getCreatedAt(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.txtTime.setText(timeAgo);

        holder.viewUnreadDot.setVisibility(notif.isRead() ? View.GONE : View.VISIBLE);
        holder.itemView.setAlpha(notif.isRead() ? 0.6f : 1.0f);

        // Styling based on severity
        int iconRes = R.drawable.ic_notification;
        int colorBg = Color.parseColor("#374151"); // Default gray
        
        if (notif.getSeverity() != null) {
            switch (notif.getSeverity().toLowerCase()) {
                case "success":
                    colorBg = Color.parseColor("#10B981"); // Green
                    // Note: Use a specific icon if available, otherwise fallback to notification
                    break;
                case "warning":
                    colorBg = Color.parseColor("#F59E0B"); // Yellow/Orange
                    break;
                case "critical":
                    colorBg = Color.parseColor("#EF4444"); // Red
                    break;
                case "info":
                default:
                    colorBg = Color.parseColor("#3B82F6"); // Blue
                    break;
            }
        }
        
        holder.cardIcon.setCardBackgroundColor(colorBg);
        holder.imgIcon.setImageResource(iconRes);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notif);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtMessage, txtTime;
        View viewUnreadDot;
        MaterialCardView cardIcon;
        ImageView imgIcon;

        ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            cardIcon = itemView.findViewById(R.id.cardIcon);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}
