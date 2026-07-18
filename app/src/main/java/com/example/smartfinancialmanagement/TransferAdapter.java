package com.example.smartfinancialmanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransferAdapter extends RecyclerView.Adapter<TransferAdapter.TransferViewHolder> {

    private final List<TransferInfo> transfers;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US);

    public TransferAdapter(List<TransferInfo> transfers) {
        this.transfers = transfers;
    }

    @NonNull
    @Override
    public TransferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer, parent, false);
        return new TransferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferViewHolder holder, int position) {
        TransferInfo info = transfers.get(position);

        holder.txtFrom.setText(info.fromAccount);
        holder.txtTo.setText(info.toAccount);
        holder.txtAmount.setText(String.format(Locale.US, "LKR %.2f", info.amount));

        if (info.note != null && !info.note.isEmpty()) {
            holder.txtNote.setText(info.note);
            holder.txtNote.setVisibility(View.VISIBLE);
        } else {
            holder.txtNote.setVisibility(View.GONE);
        }

        if (info.timestamp != null) {
            Date date = info.timestamp.toDate();
            holder.txtDate.setText(dateFormat.format(date));
        } else {
            holder.txtDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return transfers.size();
    }

    static class TransferViewHolder extends RecyclerView.ViewHolder {
        TextView txtFrom, txtTo, txtAmount, txtNote, txtDate;

        TransferViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFrom = itemView.findViewById(R.id.txtTransferFrom);
            txtTo = itemView.findViewById(R.id.txtTransferTo);
            txtAmount = itemView.findViewById(R.id.txtTransferAmount);
            txtNote = itemView.findViewById(R.id.txtTransferNote);
            txtDate = itemView.findViewById(R.id.txtTransferDate);
        }
    }

    public static class TransferInfo {
        String fromAccount;
        String toAccount;
        double amount;
        String note;
        Timestamp timestamp;

        public TransferInfo(String fromAccount, String toAccount, double amount, String note, Timestamp timestamp) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
            this.note = note;
            this.timestamp = timestamp;
        }
    }
}
