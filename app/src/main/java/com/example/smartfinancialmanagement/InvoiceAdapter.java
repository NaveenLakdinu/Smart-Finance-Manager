package com.example.smartfinancialmanagement;

import android.content.Intent; // Added missing import
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private final List<InvoiceModel> invoiceList;

    public InvoiceAdapter(List<InvoiceModel> invoiceList) {
        this.invoiceList = invoiceList;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        InvoiceModel invoice = invoiceList.get(position);

        holder.txtClientName.setText(invoice.getClientName());
        holder.txtItemAndQty.setText(String.format(Locale.getDefault(), "%s • Qty: %d", invoice.getItemName(), invoice.getQuantity()));
        holder.txtDueDate.setText(String.format("Due: %s", invoice.getPaymentDueDate()));
        holder.txtTotalAmount.setText(String.format(Locale.getDefault(), "Rs. %.2f", invoice.getGrandTotal()));

        String status = invoice.getStatus() != null ? invoice.getStatus().toLowerCase() : "pending";
        holder.txtStatusBadge.setText(status.toUpperCase());

        // Update card style statuses dynamically
        if (status.equals("paid")) {
            holder.txtStatusBadge.setTextColor(Color.parseColor("#071A33"));
            holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#00D4AA"));
        } else if (status.equals("due")) {
            holder.txtStatusBadge.setTextColor(Color.parseColor("#FFFFFF"));
            holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#FF5555"));
        } else {
            holder.txtStatusBadge.setTextColor(Color.parseColor("#F0F6FF"));
            holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#1A3050"));
        }

        // Fixed: Completed the onClickListener navigation pipeline block sequence safely
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), InvoiceDetailsActivity.class);

            // Pass fields across the intent channel
            intent.putExtra("clientName", invoice.getClientName());
            intent.putExtra("selectedBusiness", invoice.getSelectedBusiness());
            intent.putExtra("clientBRN", invoice.getClientBRN());
            intent.putExtra("itemName", invoice.getItemName());
            intent.putExtra("quantity", invoice.getQuantity());
            intent.putExtra("unitPrice", invoice.getUnitPrice());
            intent.putExtra("grandTotal", invoice.getGrandTotal());
            intent.putExtra("paymentDueDate", invoice.getPaymentDueDate());
            intent.putExtra("status", invoice.getStatus());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView txtClientName, txtItemAndQty, txtDueDate, txtTotalAmount, txtStatusBadge;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClientName = itemView.findViewById(R.id.txtClientName);
            txtItemAndQty = itemView.findViewById(R.id.txtItemAndQty);
            txtDueDate = itemView.findViewById(R.id.txtDueDate);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
        }
    }
}