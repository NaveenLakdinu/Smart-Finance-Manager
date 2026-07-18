package com.example.smartfinancialmanagement;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RecentSubscriptionAdapter extends RecyclerView.Adapter<RecentSubscriptionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Subscription subscription, int position);
        void onItemLongClick(Subscription subscription, int position);
    }

    private Context context;
    private ArrayList<Subscription> list;
    private OnItemClickListener listener;

    public RecentSubscriptionAdapter(Context context, ArrayList<Subscription> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subscription subscription = list.get(position);

        String name = subscription.getName();
        String renewDate = subscription.getRenewDate();
        String status = subscription.getStatus();
        String logoType = subscription.getLogoType();

        if (name == null || name.trim().isEmpty()) {
            name = "Subscription";
        }

        if (renewDate == null || renewDate.trim().isEmpty()) {
            renewDate = "Date not added";
        } else {
            renewDate = "Renew on " + renewDate;
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Unknown";
        }

        holder.txtSubName.setText(name);
        holder.txtRenewDate.setText(renewDate);
        holder.txtStatus.setText(status);

        if (holder.txtAmount != null) {
            holder.txtAmount.setText(String.format("LKR %,.2f", subscription.getAmount()));
        }

        setLogo(holder.txtLogo, logoType, name);
        setStatusColor(holder.txtStatus, status);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(subscription, holder.getAdapterPosition());
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(subscription, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void removeItem(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLogo, txtSubName, txtRenewDate, txtStatus, txtAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLogo = itemView.findViewById(R.id.txtLogo);
            txtSubName = itemView.findViewById(R.id.txtSubName);
            txtRenewDate = itemView.findViewById(R.id.txtRenewDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtAmount = itemView.findViewById(R.id.txtAmount);
        }
    }

    private void setLogo(TextView logo, String logoType, String name) {
        if (logoType == null) logoType = "";

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(14);

        switch (logoType.toLowerCase()) {
            case "netflix":
                logo.setText("N");
                logo.setTextColor(Color.parseColor("#E50914"));
                bg.setColor(Color.parseColor("#000000"));
                break;
            case "spotify":
                logo.setText("S");
                logo.setTextColor(Color.parseColor("#000000"));
                bg.setColor(Color.parseColor("#1DB954"));
                bg.setCornerRadius(50);
                break;
            case "youtube":
                logo.setText("▶");
                logo.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setColor(Color.parseColor("#FF0000"));
                break;
            case "chatgpt":
                logo.setText("AI");
                logo.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setColor(Color.parseColor("#000000"));
                break;
            default:
                if (name != null && name.length() > 0) {
                    logo.setText(String.valueOf(name.charAt(0)).toUpperCase());
                } else {
                    logo.setText("S");
                }
                logo.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setColor(Color.parseColor("#071A33"));
                break;
        }
        logo.setBackground(bg);
    }

    private void setStatusColor(TextView statusView, String status) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E6E8EB"));
        bg.setCornerRadius(50);
        statusView.setBackground(bg);

        if (status.equalsIgnoreCase("Active")) {
            statusView.setTextColor(Color.parseColor("#2EAD4A"));
        } else if (status.equalsIgnoreCase("Upcoming")) {
            statusView.setTextColor(Color.parseColor("#FF9800"));
        } else if (status.equalsIgnoreCase("Expired")) {
            statusView.setTextColor(Color.parseColor("#F44336"));
        } else if (status.equalsIgnoreCase("Paused")) {
            statusView.setTextColor(Color.parseColor("#607D8B"));
        } else {
            statusView.setTextColor(Color.parseColor("#333333"));
        }
    }
}
