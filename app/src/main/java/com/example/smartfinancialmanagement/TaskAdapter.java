package com.example.smartfinancialmanagement;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Using ContextCompat instead to avoid AppCompatResources error
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

@SuppressWarnings("unused") // Suppresses "Class is never used" warnings when viewing file alone
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final OnTaskCheckedChangeListener listener;

    public interface OnTaskCheckedChangeListener {
        void onCheckedChanged(Task task, boolean isChecked);
    }

    public TaskAdapter(List<Task> taskList, OnTaskCheckedChangeListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());
        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1.0f);
        }

        holder.tvDescription.setText(task.getDescription());
        holder.tvSubtasks.setText(task.getSubtaskText());
        holder.tvDueDate.setText(task.getDueLabel());
        holder.tvStatus.setText(task.getStatus());

        switch (task.getPriority()) {
            case "High":
                holder.tvPriority.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.tag_blue));
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.qa_blue_icon));
                break;
            case "Medium":
                holder.tvPriority.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.tag_green));
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pill_positive_text));
                break;
            case "Low":
                holder.tvPriority.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.tag_purple));
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.qa_purple_icon));
                break;
        }
        holder.tvPriority.setText(task.getPriority());

        int statusColor;
        int statusIcon;
        switch (task.getStatus()) {
            case "In Progress":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.qa_amber_icon);
                statusIcon = R.drawable.ic_clock;
                break;
            case "Completed":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.pill_positive_text);
                statusIcon = R.drawable.ic_check_circle;
                break;
            case "Overdue":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.danger_text);
                statusIcon = R.drawable.ic_alert_octagon;
                break;
            case "Pending":
            default:
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_dark_muted);
                statusIcon = R.drawable.ic_clock_alert;
                break;
        }
        holder.ivStatusIcon.setImageResource(statusIcon);
        holder.ivStatusIcon.setColorFilter(statusColor);
        holder.tvStatus.setTextColor(statusColor);

        if (task.getStatus().equals("Overdue")) {
            holder.tvDueDate.setTextColor(statusColor);
        } else {
            holder.tvDueDate.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_dark_secondary));
        }

        if (task.isCompleted()) {
            holder.progressContainer.setVisibility(View.GONE);
        } else {
            holder.progressContainer.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(task.getProgress());
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(statusColor));
            holder.tvProgress.setText(holder.itemView.getContext().getString(R.string.progress_format, task.getProgress()));
            holder.tvProgress.setTextColor(statusColor);
        }

        holder.cbTask.setOnCheckedChangeListener(null);
        holder.cbTask.setChecked(task.isCompleted());
        holder.cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onCheckedChanged(task, isChecked));
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTask;
        TextView tvTitle, tvDescription, tvPriority, tvProgress, tvSubtasks, tvStatus, tvDueDate;
        ImageView ivStatusIcon;
        ProgressBar progressBar;
        LinearLayout progressContainer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTask = itemView.findViewById(R.id.cbTask);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvSubtasks = itemView.findViewById(R.id.tvSubtasks);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressContainer = itemView.findViewById(R.id.progressContainer);
        }
    }
}
