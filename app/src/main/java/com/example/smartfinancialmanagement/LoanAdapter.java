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

public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {

    private Context context;
    private ArrayList<Loan> loanList;
    private OnLoanClickListener listener;

    public interface OnLoanClickListener {
        void onLoanClick(Loan loan);
        void onDeleteClick(Loan loan);
    }

    public LoanAdapter(Context context, ArrayList<Loan> loanList, OnLoanClickListener listener) {
        this.context = context;
        this.loanList = loanList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_loan, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
        Loan loan = loanList.get(position);
        holder.txtLoanName.setText(loan.getLoanName());
        holder.txtLoanAmount.setText(CurrencyHelper.formatMoney(context, loan.getPrincipalAmount()));
        holder.txtLoanEMI.setText(CurrencyHelper.formatMoney(context, loan.getMonthlyEmi()));

        holder.itemView.setOnClickListener(v -> listener.onLoanClick(loan));
        holder.btnDeleteLoan.setOnClickListener(v -> listener.onDeleteClick(loan));
    }

    @Override
    public int getItemCount() {
        return loanList.size();
    }

    public static class LoanViewHolder extends RecyclerView.ViewHolder {
        TextView txtLoanName, txtLoanAmount, txtLoanEMI;
        ImageView btnDeleteLoan;

        public LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLoanName = itemView.findViewById(R.id.txtLoanName);
            txtLoanAmount = itemView.findViewById(R.id.txtLoanAmount);
            txtLoanEMI = itemView.findViewById(R.id.txtLoanEMI);
            btnDeleteLoan = itemView.findViewById(R.id.btnDeleteLoan);
        }
    }
}
