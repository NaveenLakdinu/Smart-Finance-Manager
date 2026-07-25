package com.example.smartfinancialmanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BillSummaryAdapter extends RecyclerView.Adapter<BillSummaryAdapter.ViewHolder> {

    // Interface to communicate item deletions back to the Activity
    public interface OnItemRemovedListener {
        void onItemRemoved();
    }

    private List<BillReportItem> items;
    private OnItemRemovedListener listener;

    public BillSummaryAdapter(List<BillReportItem> items, OnItemRemovedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary_bill, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillReportItem item = items.get(position);
        holder.name.setText(item.getBillName());
        holder.month.setText("Month: " + item.getTargetMonth());
        holder.amount.setText(String.format(java.util.Locale.getDefault(), "Rs. %.2f", item.getAmount()));

        // Remove button click functionality
        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, items.size());

                // Notify ReportSummaryActivity to update button state
                if (listener != null) {
                    listener.onItemRemoved();
                }
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, month, amount;
        ImageView btnRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtSummaryName);
            month = itemView.findViewById(R.id.txtSummaryMonth);
            amount = itemView.findViewById(R.id.txtSummaryAmount);
            btnRemove = itemView.findViewById(R.id.btnRemoveStagedItem);
        }
    }
}