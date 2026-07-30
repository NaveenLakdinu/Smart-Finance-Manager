package com.example.smartfinancialmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartfinancialmanagement.model.OTEntry;
import java.util.List;
import java.util.Locale;

public class OTEntryAdapter extends RecyclerView.Adapter<OTEntryAdapter.ViewHolder> {

    private final Context context;
    private final List<OTEntry> entryList;
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDeleteClick(OTEntry entry);
    }

    public OTEntryAdapter(Context context, List<OTEntry> entryList, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.entryList = entryList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ot_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OTEntry entry = entryList.get(position);

        holder.tvDate.setText(entry.date);
        holder.tvTime.setText(String.format(Locale.US, "%s - %s", entry.startTime, entry.endTime));
        holder.tvHoursRate.setText(String.format(Locale.US, "%.1f hrs @ Rs %.2f/hr", entry.otHours, entry.otRate));
        holder.tvAmount.setText(String.format(Locale.US, "Rs %.2f", entry.otPay));

        holder.btnDelete.setVisibility(View.VISIBLE); // Always show delete button for now
        
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvHoursRate, tvAmount;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvHoursRate = itemView.findViewById(R.id.tv_hours_rate);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
