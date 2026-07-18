package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseManagementActivity extends AppCompatActivity {

    private static final String TAG = "ExpenseManagement";
    private TextView txtTotalExpenses;
    private LinearLayout layoutBusinessCardsContainer;
    private Button btnOpenAddExpense;
    private RecyclerView recyclerExpenses;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<String> businessList = new ArrayList<>();

    // Split lists to preserve all month logs vs dynamic UI active selections
    private List<DocumentSnapshot> allMonthExpenseDocs = new ArrayList<>();
    private List<DocumentSnapshot> filteredExpenseDocs = new ArrayList<>();

    private String selectedWorkspaceFilter = "ALL"; // Tracks active card selection
    private Map<String, Double> businessTotalsMap = new HashMap<>();

    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_management);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String currentUserId = user.getUid();

        // 💡 Server-Side Filter: Load only your specific business configurations
        db.collection("businesses")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    businessList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String bName = doc.getString("businessName");
                        if (bName != null) businessList.add(bName);
                    }
                    syncDataPipeline(currentUserId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching profiles: " + e.getMessage()));
    }

    private void syncDataPipeline(String currentUserId) {
        // 💡 Server-Side Filter: Only fetch expense documents mapped to this userId
        db.collection("expenses")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    allMonthExpenseDocs.clear();
                    businessTotalsMap.clear();

                    for (String b : businessList) businessTotalsMap.put(b, 0.0);

                    double currentMonthHeroGrandTotal = 0.0;
                    String currentMonthToken = yearMonthFormat.format(Calendar.getInstance().getTime());

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Double amount = doc.getDouble("amount");
                        String bName = doc.getString("selectedBusiness");
                        String dateStr = doc.getString("date");

                        if (amount != null && bName != null) {
                            if (businessTotalsMap.containsKey(bName)) {
                                businessTotalsMap.put(bName, businessTotalsMap.get(bName) + amount);
                            }

                            if (dateStr != null && dateStr.startsWith(currentMonthToken)) {
                                allMonthExpenseDocs.add(doc);
                                currentMonthHeroGrandTotal += amount;
                            }
                        }
                    }

                    txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthHeroGrandTotal));

                    // Build cards layout UI row
                    populateBusinessRowCards();
                    // Render list history items based on filter bounds
                    applyRecyclerFilter();
                });
    }

    private void populateBusinessRowCards() {
        layoutBusinessCardsContainer.removeAllViews();

        // Master "All Workspaces" selection card
        double overallTotal = 0.0;
        for (double val : businessTotalsMap.values()) {
            overallTotal += val;
        }
        createWorkspaceFilterCard("ALL", overallTotal);

        // Individual workspace selection cards
        for (Map.Entry<String, Double> entry : businessTotalsMap.entrySet()) {
            createWorkspaceFilterCard(entry.getKey(), entry.getValue());
        }
    }

    private void createWorkspaceFilterCard(String title, double value) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(380, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 16, 0);
        card.setLayoutParams(params);
        card.setRadius(24f);
        card.setStrokeWidth(0);

        // 💡 Highlight if active selection card
        if (selectedWorkspaceFilter.equalsIgnoreCase(title)) {
            card.setCardBackgroundColor(Color.parseColor("#00D4AA")); // Theme Accent Highlight
        } else {
            card.setCardBackgroundColor(Color.parseColor("#1A3050")); // Standard Off dark
        }

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(24, 24, 24, 24);

        TextView lblName = new TextView(this);
        lblName.setText(title.toUpperCase());
        lblName.setTextColor(selectedWorkspaceFilter.equalsIgnoreCase(title) ? Color.parseColor("#071A33") : Color.parseColor("#7A9CC0"));
        lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        lblName.setSingleLine(true);
        lblName.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView lblValue = new TextView(this);
        lblValue.setText(String.format(Locale.getDefault(), "Rs. %,.0f", value));
        lblValue.setTextColor(selectedWorkspaceFilter.equalsIgnoreCase(title) ? Color.parseColor("#071A33") : Color.parseColor("#FF5555"));
        lblValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        lblValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        lblValue.setPadding(0, 8, 0, 0);

        innerLayout.addView(lblName);
        innerLayout.addView(lblValue);
        card.addView(innerLayout);

        // 💡 Card Action Handler: Filter the history listing when pressed
        card.setOnClickListener(v -> {
            selectedWorkspaceFilter = title;
            populateBusinessRowCards(); // Rebuild for UI states swap
            applyRecyclerFilter();
        });

        layoutBusinessCardsContainer.addView(card);
    }

    private void applyRecyclerFilter() {
        filteredExpenseDocs.clear();

        for (DocumentSnapshot doc : allMonthExpenseDocs) {
            String bName = doc.getString("selectedBusiness");
            if (selectedWorkspaceFilter.equals("ALL") || (bName != null && bName.equalsIgnoreCase(selectedWorkspaceFilter))) {
                filteredExpenseDocs.add(doc);
            }
        }

        setupHistoryRecycler();
    }

    private void showAddExpenseDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String currentUserId = user.getUid();

        if (businessList.isEmpty()) {
            Toast.makeText(this, "Create a business workspace profile baseline configuration first", Toast.LENGTH_SHORT).show();
            return;
        }

        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);

        Spinner spinBus = customView.findViewById(R.id.dialogSpinnerBusiness);
        Spinner spinCat = customView.findViewById(R.id.SpinnerCategory);
        EditText edtAmt = customView.findViewById(R.id.dialogEdtAmount);
        Button btnSave = customView.findViewById(R.id.dialogBtnSubmit);

        spinBus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, businessList));
        String[] categories = {"Utilities", "Rent & Space", "Salaries", "Inventory Stock", "Marketing", "Other"};
        spinCat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
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
            record.put("userId", currentUserId); // 💡 Securely save user UID with the document entry

            db.collection("expenses").add(record).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Expense logged successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                syncDataPipeline(currentUserId);
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
                DocumentSnapshot doc = filteredExpenseDocs.get(pos);
                h.t1.setText(String.format("%s • %s", doc.getString("selectedBusiness"), doc.getString("category")));
                h.t1.setTextColor(Color.parseColor("#F0F6FF"));
                h.t2.setText(String.format(Locale.getDefault(), "Rs. %,.2f (Logged: %s) [Hold item to delete entry]", doc.getDouble("amount"), doc.getString("date")));
                h.t2.setTextColor(Color.parseColor("#7A9CC0"));

                h.itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Correction Warning")
                            .setMessage("Delete this expense row item entry permanently?")
                            .setPositiveButton("Delete", (d, w) -> {
                                db.collection("expenses").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ExpenseManagementActivity.this, "Entry removed safely", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) syncDataPipeline(user.getUid());
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }

            @Override
            public int getItemCount() { return filteredExpenseDocs.size(); }
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