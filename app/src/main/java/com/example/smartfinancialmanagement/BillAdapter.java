package com.example.smartfinancialmanagement;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private Context context;
    private List<UtilityBill> billList; // FIX: Using clean unified object model collection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public BillAdapter(Context context, List<UtilityBill> billList) {
        this.context = context;
        this.billList = billList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        UtilityBill bill = billList.get(position); // FIX: Safe extraction mapping directly
        String documentId = bill.getId();

        // Map data safely
        holder.txtName.setText(bill.getBillName()); // FIX: Map getters to match UtilityBill.java properties
        holder.txtAccNo.setText("Acc: " + bill.getAccountNo());
        holder.txtDate.setText("Due Day: " + bill.getPaymentDate()); // FIX: Use getPaymentDate()

        // Dynamic category icon assignment
        if (bill.getCategory() != null) {
            switch (bill.getCategory()) {
                case "Electricity":
                    holder.imgIcon.setImageResource(android.R.drawable.ic_menu_compass);
                    break;
                case "Water":
                    holder.imgIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                    break;
                case "Telephone":
                case "Internet":
                    holder.imgIcon.setImageResource(android.R.drawable.ic_menu_call);
                    break;
                default:
                    holder.imgIcon.setImageResource(android.R.drawable.ic_menu_agenda);
                    break;
            }
        }

        // Row click setup to open the Update Form
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdateBillActivity.class);
            intent.putExtra("BILL_ID", documentId);
            intent.putExtra("BILL_NAME", bill.getBillName());
            intent.putExtra("BILL_ACC", bill.getAccountNo());
            intent.putExtra("BILL_CAT", bill.getCategory());
            intent.putExtra("BILL_DATE", bill.getPaymentDate());
            context.startActivity(intent);
        });

        // Delete button execution block
        holder.btnDelete.setOnClickListener(v -> {
            if (documentId == null) return;

            // FIX: Target directory node shifted to the active "utilityBill" collection path
            db.collection("utilityBill").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Bill Deleted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtAccNo, txtDate;
        ImageView imgIcon, btnDelete;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtBillItemName);
            txtAccNo = itemView.findViewById(R.id.txtBillItemAccNo);
            txtDate = itemView.findViewById(R.id.txtBillItemDate);
            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);
            btnDelete = itemView.findViewById(R.id.btnDeleteBill);
        }
    }
}