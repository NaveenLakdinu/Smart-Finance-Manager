package com.example.smartfinancialmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;

public class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UtilityBill> list;
    private OnUtilityClickListener listener;

    // FIX: Added onCardClick so the activity knows when to open the management screen
    public interface OnUtilityClickListener {
        void onDeleteClick(UtilityBill bill);
        void onCardClick(UtilityBill bill);
    }

    public UtilityAdapter(Context context, ArrayList<UtilityBill> list, OnUtilityClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_utility_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UtilityBill bill = list.get(position);

        holder.txtBillName.setText(bill.getBillName());
        holder.txtBillDetails.setText(String.format(Locale.US, "Due %s",
                bill.getPaymentDate(), bill.getStatus()));

        // Set Category Icon Emoji
        String cat = bill.getCategory() != null ? bill.getCategory().toLowerCase() : "";
        if (cat.contains("electric")) {
            holder.txtCategoryIcon.setText("💡");
        } else if (cat.contains("water")) {
            holder.txtCategoryIcon.setText("🚰");
        } else if (cat.contains("phone") || cat.contains("tele")) {
            holder.txtCategoryIcon.setText("📞");
        } else if (cat.contains("internet") || cat.contains("wifi")) {
            holder.txtCategoryIcon.setText("🌐");
        } else if (cat.contains("tv") || cat.contains("television")) {
            holder.txtCategoryIcon.setText("📺");
        } else if (cat.contains("rent")) {
            holder.txtCategoryIcon.setText("🏠");
        } else {
            holder.txtCategoryIcon.setText("📄");
        }

        // Fetch Current User
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Manage action buttons visibility based on ownership rules
        if (bill.getUserId() != null && bill.getUserId().equals(currentUserId)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE); // FIX: Enabled the edit icon view visual state

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(bill);
                }
            });

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(bill);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
        }

        // FIX: Makes the whole outer card layout act as an update redirection button
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(bill);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryIcon, txtBillName, txtBillDetails;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryIcon = itemView.findViewById(R.id.txtCategoryIcon);
            txtBillName = itemView.findViewById(R.id.txtBillName);
            txtBillDetails = itemView.findViewById(R.id.txtBillDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}