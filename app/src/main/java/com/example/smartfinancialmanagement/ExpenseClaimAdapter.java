package com.example.smartfinancialmanagement;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ExpenseClaimAdapter extends RecyclerView.Adapter<ExpenseClaimAdapter.ClaimViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ExpenseClaim claim, int position);
        void onItemLongClick(ExpenseClaim claim, int position);
        void onApproveClick(ExpenseClaim claim, int position);
        void onRejectClick(ExpenseClaim claim, int position);
    }

    private final List<ExpenseClaim> claimList;
    private OnItemClickListener listener;

    public ExpenseClaimAdapter(List<ExpenseClaim> claimList) {
        this.claimList = claimList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_claim, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        ExpenseClaim claim = claimList.get(position);

        holder.txtCategoryIcon.setText(claim.getCategoryIcon());
        holder.txtCategoryIcon.setBackgroundResource(R.drawable.bg_function_icon_dark);
        holder.txtCategoryIcon.getBackground().setTint(claim.getCategoryIconBg());

        holder.txtClaimTitle.setText(claim.getTitle());
        holder.txtClaimMeta.setText(String.format("%s \u2022 %s", claim.getCategoryLabel(), claim.getExpenseDate()));

        holder.txtStatusPill.setText(claim.getStatusLabel());
        holder.txtStatusPill.setTextColor(claim.getStatusColor());
        holder.txtStatusPill.setBackgroundColor(claim.getStatusBgColor());

        holder.txtClaimAmount.setText(claim.getFormattedAmount());

        if ("PENDING".equalsIgnoreCase(claim.getStatus())) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            holder.actionButtonsLayout.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApproveClick(claim, position);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onRejectClick(claim, position);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(claim, position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(claim, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }

    static class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryIcon, txtClaimTitle, txtClaimMeta, txtStatusPill, txtClaimAmount;
        View actionButtonsLayout;
        View btnApprove, btnReject;

        public ClaimViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryIcon = itemView.findViewById(R.id.txtCategoryIcon);
            txtClaimTitle = itemView.findViewById(R.id.txtClaimTitle);
            txtClaimMeta = itemView.findViewById(R.id.txtClaimMeta);
            txtStatusPill = itemView.findViewById(R.id.txtStatusPill);
            txtClaimAmount = itemView.findViewById(R.id.txtClaimAmount);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
