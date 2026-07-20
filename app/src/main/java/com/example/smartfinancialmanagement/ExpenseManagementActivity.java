package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.widget.Toast;
import android.util.Log;

public class ExpenseManagementActivity extends AppCompatActivity {

    private TextView btnBack;
    private Spinner spinnerExpenseCategory;
    private EditText edtExpenseAmount;
    private EditText edtExpenseDate;
    private EditText edtExpenseDescription;
    private Button btnAddExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_management);

        // 1. Hook up the interactive layout components
        initializeViews();

        // 2. Setup standard static dropdown list items
        setupSpinner();

        // 3. Keep local OS UI components like the native date picker calendar
        setupDatePicker();

        // 4. Attach standard click listeners
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDate = findViewById(R.id.edtExpenseDate);
        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        btnAddExpense = findViewById(R.id.btnAddExpense);
    }

    private void setupSpinner() {
        String[] expenseCategories = {
                "Utility Bill",
                "Loan Payment",
                "Subscription",
                "Inventory Purchase",
                "Employee Salary",
                "Marketing",
                "Transport",
                "Equipment",
                "Office Rent",
                "Other Expense"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                expenseCategories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerExpenseCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        edtExpenseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ExpenseManagementActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtExpenseDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Simple navigation handling back to dashboard
        btnBack.setOnClickListener(v -> finish());

        // Standard frontend validation logic before submissions
        btnAddExpense.setOnClickListener(v -> {
            String amount = edtExpenseAmount.getText().toString().trim();
            String date = edtExpenseDate.getText().toString().trim();

            if (amount.isEmpty()) {
                edtExpenseAmount.setError("Enter amount");
                return;
            }

            if (date.isEmpty()) {
                edtExpenseDate.setError("Select date");
                return;
            }

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
    }
}