package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.widget.Toast;
import android.util.Log;

public class ExpenseManagementActivity extends AppCompatActivity {

    private TextView txtTotalExpenses;
    private LinearLayout layoutBusinessCardsContainer;
    private Button btnOpenAddExpense;
    private RecyclerView recyclerExpenses;

    private FirebaseFirestore db;
    private List<String> businessList = new ArrayList<>();
    private List<DocumentSnapshot> currentMonthExpenseDocs = new ArrayList<>();
    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_management);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        loadBusinessProfiles();
    }

    private void initializeViews() {
        txtTotalExpenses = findViewById(R.id.txtTotalExpenses);
        layoutBusinessCardsContainer = findViewById(R.id.layoutBusinessCardsContainer);
        btnOpenAddExpense = findViewById(R.id.btnOpenAddExpense);
        recyclerExpenses = findViewById(R.id.recyclerExpenses);

        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnOpenAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void loadBusinessProfiles() {
        db.collection("businesses").get().addOnSuccessListener(snapshots -> {
            businessList.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String bName = doc.getString("businessName");
                if (bName != null) businessList.add(bName);
            }
            syncDataPipeline();
        });
    }

    private void syncDataPipeline() {
        db.collection("expenses").get().addOnSuccessListener(snapshots -> {
            currentMonthExpenseDocs.clear();

            // Tracks combined expenses for JUST the current calendar month
            double currentMonthHeroGrandTotal = 0.0;

            // Map tracking structures for summing each business's overall expenses for small cards
            Map<String, Double> businessTotalsMap = new HashMap<>();
            for (String b : businessList) businessTotalsMap.put(b, 0.0);

            // Get current year and month token string (e.g., "2026-06")
            String currentMonthToken = yearMonthFormat.format(Calendar.getInstance().getTime());

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Double amount = doc.getDouble("amount");
                String bName = doc.getString("selectedBusiness");
                String dateStr = doc.getString("date");

                if (amount != null && bName != null) {
                    // Accumulate overall expenses for the small cards mapping
                    if (businessTotalsMap.containsKey(bName)) {
                        businessTotalsMap.put(bName, businessTotalsMap.get(bName) + amount);
                    }

                    // Filter logs falling strictly inside current calendar month metric boundaries
                    if (dateStr != null && dateStr.startsWith(currentMonthToken)) {
                        currentMonthExpenseDocs.add(doc);

                        // Add to Hero Card total ONLY if it belongs to the current month
                        currentMonthHeroGrandTotal += amount;
                    }
                }
            }

            // Hero Card view displays ONLY this month's calculations aggregate balance
            txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %.2f", currentMonthHeroGrandTotal));

            saveExpenseToFirestore(amount, date, spinnerExpenseCategory.getSelectedItem().toString(), edtExpenseDescription.getText().toString());
            
        });
    }

    private void saveExpenseToFirestore(String amountStr, String date, String category, String description) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        double amount = Double.parseDouble(amountStr);

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", amount);
        expense.put("date", date);
        expense.put("category", category);
        expense.put("description", description);
        expense.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("expenses")
            .add(expense)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Expense Saved", Toast.LENGTH_SHORT).show();
                checkBudgetLimits(userId, amount);
                clearFields();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show());
    }

    private void checkBudgetLimits(String userId, double newExpenseAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Fetch budget
        db.collection("users").document(userId).collection("budgetPlans")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(budgetDocs -> {
                if (!budgetDocs.isEmpty()) {
                    BudgetModel budget = budgetDocs.getDocuments().get(0).toObject(BudgetModel.class);
                    if (budget != null) {
                        double monthlyLimit = budget.getMonthlyBudget();
                        
                        // Fetch all expenses to get a total (simplified logic)
                        db.collection("users").document(userId).collection("expenses")
                            .get()
                            .addOnSuccessListener(expenseDocs -> {
                                double totalExpenses = 0;
                                for (QueryDocumentSnapshot doc : expenseDocs) {
                                    Double amt = doc.getDouble("amount");
                                    if (amt != null) totalExpenses += amt;
                                }
                                
                                NotificationRepository repo = new NotificationRepository();
                                
                                if (totalExpenses > monthlyLimit) {
                                    NotificationModel notif = new NotificationModel(
                                        null, userId, "budget_critical", "Budget Exceeded! 🚨", 
                                        "You have exceeded your monthly budget of " + CurrencyHelper.formatMoney(this, monthlyLimit), 
                                        "critical", "BudgetPlanner", "budget_" + budget.getDocumentId(), false, 
                                        System.currentTimeMillis(), "BudgetPlannerActivity"
                                    );
                                    repo.checkAndCreateDuplicateSafe(notif);
                                } else if (totalExpenses > monthlyLimit * 0.8) {
                                    NotificationModel notif = new NotificationModel(
                                        null, userId, "budget_warning", "Budget Warning ⚠️", 
                                        "You have spent over 80% of your monthly budget.", 
                                        "warning", "BudgetPlanner", "budget_" + budget.getDocumentId(), false, 
                                        System.currentTimeMillis(), "BudgetPlannerActivity"
                                    );
                                    repo.checkAndCreateDuplicateSafe(notif);
                                }
                            });
                    }
                }
            });
    }

    private void clearFields() {
        edtExpenseAmount.setText("");
        edtExpenseDate.setText("");
        edtExpenseDescription.setText("");
        spinnerExpenseCategory.setSelection(0);
            populateBusinessRowCards(businessTotalsMap);
            setupHistoryRecycler();
        });
    }

    private void populateBusinessRowCards(Map<String, Double> dataMap) {
        layoutBusinessCardsContainer.removeAllViews();

        for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(380, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            card.setLayoutParams(params);
            card.setRadius(24f);
            card.setStrokeWidth(0);
            card.setCardBackgroundColor(Color.parseColor("#1A3050"));

            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(20, 20, 20, 20);

            TextView lblName = new TextView(this);
            lblName.setText(entry.getKey().toUpperCase());
            lblName.setTextColor(Color.parseColor("#7A9CC0"));
            lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            lblName.setSingleLine(true);
            lblName.setEllipsize(android.text.TextUtils.TruncateAt.END);

            TextView lblValue = new TextView(this);
            lblValue.setText(String.format(Locale.getDefault(), "Rs. %,.0f", entry.getValue()));
            lblValue.setTextColor(Color.parseColor("#FF5555")); // Coral Red for expenses indicator
            lblValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            lblValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            lblValue.setPadding(0, 8, 0, 0);

            innerLayout.addView(lblName);
            innerLayout.addView(lblValue);
            card.addView(innerLayout);
            layoutBusinessCardsContainer.addView(card);
        }
    }

    private void showAddExpenseDialog() {
        if (businessList.isEmpty()) {
            Toast.makeText(this, "Create a business workspace profile baseline configuration first", Toast.LENGTH_SHORT).show();
            return;
        }

        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);

        // Found inside customView to avoid Symbol Resolution Errors
        Spinner spinBus = customView.findViewById(R.id.dialogSpinnerBusiness);
        Spinner spinCat = customView.findViewById(R.id.SpinnerCategory);
        EditText edtAmt = customView.findViewById(R.id.dialogEdtAmount);
        Button btnSave = customView.findViewById(R.id.dialogBtnSubmit);

        spinBus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, businessList));
        String[] categories = {"Utilities", "Rent & Space", "Salaries", "Inventory Stock", "Marketing", "Other"};
        spinCat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setView(customView)
                .create();

        btnSave.setOnClickListener(v -> {
            String amtStr = edtAmt.getText().toString().trim();
            if (amtStr.isEmpty()) {
                Toast.makeText(this, "Amount parameter space required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> record = new HashMap<>();
            record.put("selectedBusiness", spinBus.getSelectedItem().toString());
            record.put("category", spinCat.getSelectedItem().toString());
            record.put("amount", Double.parseDouble(amtStr));
            record.put("date", fullDateFormat.format(Calendar.getInstance().getTime()));

            db.collection("expenses").add(record).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Expense logged successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                syncDataPipeline();
            });
        });

        dialog.show();
    }

    private void setupHistoryRecycler() {
        recyclerExpenses.setAdapter(new RecyclerView.Adapter<HistoryViewHolder>() {
            @NonNull
            @Override
            public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                View card = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
                return new HistoryViewHolder(card);
            }

            @Override
            public void onBindViewHolder(@NonNull HistoryViewHolder h, int pos) {
                DocumentSnapshot doc = currentMonthExpenseDocs.get(pos);
                h.t1.setText(String.format("%s • %s", doc.getString("selectedBusiness"), doc.getString("category")));
                h.t1.setTextColor(Color.parseColor("#F0F6FF"));
                h.t2.setText(String.format(Locale.getDefault(), "Rs. %.2f (Logged: %s) [Hold item to delete entry]", doc.getDouble("amount"), doc.getString("date")));
                h.t2.setTextColor(Color.parseColor("#7A9CC0"));

                // Correction long-press gesture hook
                h.itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(v.getContext(), R.style.Theme_SmartFinance_Dialog)
                            .setTitle("Correction Warning")
                            .setMessage("Delete this expense row item entry permanently?")
                            .setPositiveButton("Delete", (d, w) -> {
                                db.collection("expenses").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ExpenseManagementActivity.this, "Entry removed safely", Toast.LENGTH_SHORT).show();
                                    syncDataPipeline();
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }

            @Override
            public int getItemCount() { return currentMonthExpenseDocs.size(); }
        });
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView t1, t2;
        HistoryViewHolder(View v) {
            super(v);
            t1 = v.findViewById(android.R.id.text1);
            t2 = v.findViewById(android.R.id.text2);
        }
    }
}