package com.example.smartfinancialmanagement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * SavingGoalAdapter — RecyclerView adapter for goal card items.
 * Used in both SavingListActivity and SavingManagerActivity.
 * Mirrors the Compose GoalListCard / ActiveGoalCard UI.
 */
public class SavingGoalAdapter extends RecyclerView.Adapter<SavingGoalAdapter.ViewHolder> {

    public enum GoalStatus { ONGOING, COMPLETED, INCOMPLETE }

    public static class GoalItem {
        public String id;
        public String name;
        public double target;
        public double current;
        public GoalStatus status;

        public GoalItem(String id, String name, double target, double current, GoalStatus status) {
            this.id = id;
            this.name = name;
            this.target = target;
            this.current = current;
            this.status = status;
        }

        public int percent() {
            if (target <= 0) return 0;
            return (int) Math.min(100.0, (current / target) * 100.0);
        }

        public double remaining() {
            return Math.max(0.0, target - current);
        }
    }

    public interface Listener {
        void onGoalClick(GoalItem goal);
        void onEdit(GoalItem goal);
        void onDelete(GoalItem goal);
    }

    private List<GoalItem> goals;
    private final Listener listener;
    private final Context context;

    public SavingGoalAdapter(Context context, List<GoalItem> goals, Listener listener) {
        this.context = context;
        this.goals = goals;
        this.listener = listener;
    }

    public void setGoals(List<GoalItem> goals) {
        this.goals = goals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saving_goal, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        GoalItem goal = goals.get(position);
        h.tvGoalName.setText(goal.name);
        h.tvTarget.setText(money(goal.target));
        h.tvCurrent.setText(money(goal.current));
        h.tvRemaining.setText("Remaining: " + money(goal.remaining()));
        h.tvPercent.setText(goal.percent() + "%");

        // Progress bar
        int pct = goal.percent();
        ViewGroup.LayoutParams lp = h.progressBar.getLayoutParams();
        // We use a post to get the parent width, then set width proportionally
        h.progressBar.post(() -> {
            View parent = (View) h.progressBar.getParent();
            int parentWidth = parent.getWidth();
            lp.width = Math.max(8, (int) (parentWidth * (pct / 100f)));
            h.progressBar.setLayoutParams(lp);
        });

        // Status badge
        switch (goal.status) {
            case COMPLETED:
                h.tvStatus.setText("Completed");
                h.tvStatus.setTextColor(Color.parseColor("#FFC857")); // gold
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                break;
            case INCOMPLETE:
                h.tvStatus.setText("Incomplete");
                h.tvStatus.setTextColor(Color.parseColor("#FF6B7A")); // coral
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_incomplete);
                break;
            default:
                h.tvStatus.setText("Ongoing");
                h.tvStatus.setTextColor(Color.parseColor("#2FE0AC")); // mint
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_ongoing);
                break;
        }

        h.itemView.setOnClickListener(v -> listener.onGoalClick(goal));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(goal));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(goal));
    }

    @Override
    public int getItemCount() { return goals == null ? 0 : goals.size(); }

    private String money(double n) {
        return CurrencyHelper.formatMoney(context, n);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvTarget, tvCurrent, tvRemaining, tvPercent, tvStatus;
        View progressBar;
        TextView btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName  = itemView.findViewById(R.id.tvGoalName);
            tvTarget    = itemView.findViewById(R.id.tvTarget);
            tvCurrent   = itemView.findViewById(R.id.tvCurrent);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);
            tvPercent   = itemView.findViewById(R.id.tvPercent);
            tvStatus    = itemView.findViewById(R.id.tvStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnEdit     = itemView.findViewById(R.id.btnEdit);
            btnDelete   = itemView.findViewById(R.id.btnDelete);
        }
    }
}
