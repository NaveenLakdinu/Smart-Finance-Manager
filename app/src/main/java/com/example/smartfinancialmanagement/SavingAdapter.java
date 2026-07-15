package com.example.smartfinancialmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SavingAdapter extends RecyclerView.Adapter<SavingAdapter.SavingViewHolder> {

    private Context context;
    private List<SavingModel> savingList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(SavingModel savingModel);
        void onDeleteClick(SavingModel savingModel);
        void onViewClick(SavingModel savingModel);
    }

    public SavingAdapter(Context context, List<SavingModel> savingList, OnItemClickListener listener) {
        this.context = context;
        this.savingList = savingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saving_goal, parent, false);
        return new SavingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingViewHolder holder, int position) {
        SavingModel saving = savingList.get(position);

        holder.txtSavingTitle.setText(saving.getSavingTitle());
        holder.txtTargetAmount.setText(String.format("$%.2f", saving.getTargetAmount()));
        holder.txtCurrentAmount.setText(String.format("$%.2f", saving.getCurrentAmount()));

        double remaining = saving.getTargetAmount() - saving.getCurrentAmount();
        if (remaining < 0) remaining = 0;
        holder.txtRemainingAmount.setText(String.format("Remaining: $%.2f", remaining));

        int progress = 0;
        if (saving.getTargetAmount() > 0) {
            progress = (int) ((saving.getCurrentAmount() / saving.getTargetAmount()) * 100);
        }
        if (progress > 100) progress = 100;
        
        holder.progressBar.setProgress(progress);
        holder.txtProgressPercent.setText(progress + "%");

        holder.txtStatus.setText(saving.getStatus() != null ? saving.getStatus() : "Active");

        holder.btnEditSaving.setOnClickListener(v -> listener.onEditClick(saving));
        holder.btnDeleteSaving.setOnClickListener(v -> listener.onDeleteClick(saving));
        holder.itemView.setOnClickListener(v -> listener.onViewClick(saving));
    }

    @Override
    public int getItemCount() {
        return savingList.size();
    }

    public static class SavingViewHolder extends RecyclerView.ViewHolder {
        TextView txtSavingTitle, txtTargetAmount, txtCurrentAmount, txtRemainingAmount, txtProgressPercent, txtStatus;
        ProgressBar progressBar;
        ImageView btnEditSaving, btnDeleteSaving;

        public SavingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSavingTitle = itemView.findViewById(R.id.txtSavingTitle);
            txtTargetAmount = itemView.findViewById(R.id.txtTargetAmount);
            txtCurrentAmount = itemView.findViewById(R.id.txtCurrentAmount);
            txtRemainingAmount = itemView.findViewById(R.id.txtRemainingAmount);
            txtProgressPercent = itemView.findViewById(R.id.txtProgressPercent);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnEditSaving = itemView.findViewById(R.id.btnEditSaving);
            btnDeleteSaving = itemView.findViewById(R.id.btnDeleteSaving);
        }
    }
}
