package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import android.widget.EditText;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class MultiAccountDashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtCurrentAccountName, txtAccountBalance, txtAccountNumber;
    private LinearLayout btnSwitchAccount;
    private View btnTopLogout;
    private MaterialCardView cardTransfer, cardStatements, cardLoanManager, cardCards, cardAddAccount;
    private MaterialCardView cardSubscriptionManager, cardSavingManager, cardUtilityManager;

    // Simulated account data
private List<AccountInfo> accountsList = new ArrayList<>();
private int currentAccountIndex = 0;

// Simple holder for account info
private static class AccountInfo {
    String name;
    double balance;
    String number;
    AccountInfo(String name, double balance, String number) {
        this.name = name;
        this.balance = balance;
        this.number = number;
    }
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_account_dashboard);

initViews();
setupUserDetails();
loadAccountsFromFirestore();
        
        btnSwitchAccount.setOnClickListener(v -> showAccountSwitchDialog());
        
        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        setupSecurityButton();
        setupSavingsWidget();
    }

    private void initViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtCurrentAccountName = findViewById(R.id.txtCurrentAccountName);
        txtAccountBalance = findViewById(R.id.txtAccountBalance);
        txtAccountNumber = findViewById(R.id.txtAccountNumber);
        btnSwitchAccount = findViewById(R.id.btnSwitchAccount);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        cardTransfer = findViewById(R.id.cardTransfer);
        cardStatements = findViewById(R.id.cardStatements);
        cardLoanManager = findViewById(R.id.cardLoanManager);
        cardCards = findViewById(R.id.cardCards);
        cardAddAccount = findViewById(R.id.cardAddAccount);
        cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardUtilityManager = findViewById(R.id.cardUtilityManager);

        setupActionCards();
    }

    private void setupActionCards() {
        cardTransfer.setOnClickListener(v -> Toast.makeText(this, "Transfer - Coming Soon", Toast.LENGTH_SHORT).show());
        cardStatements.setOnClickListener(v -> Toast.makeText(this, "Statements - Coming Soon", Toast.LENGTH_SHORT).show());
        cardCards.setOnClickListener(v -> Toast.makeText(this, "Card Management - Coming Soon", Toast.LENGTH_SHORT).show());
        cardAddAccount.setOnClickListener(v -> Toast.makeText(this, "Add New Account - Coming Soon", Toast.LENGTH_SHORT).show());

        cardLoanManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoanFormActivity.class);
            startActivity(intent);
        });
        cardSubscriptionManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });
        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingManagerActivity.class);
            startActivity(intent);
        });
        cardUtilityManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, UtilityManagerActivity.class);
            startActivity(intent);
        });
    }

    private void setupUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        txtGreeting.setText(getGreetingText());

        if (user != null && user.getEmail() != null) {
            txtProfileLetter.setText(String.valueOf(user.getEmail().charAt(0)).toUpperCase());
        }
    }

    private String getGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Good Morning 👋";
        else if (hour >= 12 && hour < 17) return "Good Afternoon ☀️";
        else if (hour >= 17 && hour < 21) return "Good Evening 🌙";
        else return "Good Night ✨";
    }

private void updateAccountUI() {
    if (accountsList.isEmpty()) {
        txtCurrentAccountName.setText("No Account");
        txtAccountBalance.setText("LKR 0.00");
        txtAccountNumber.setText("----");
    } else {
        AccountInfo info = accountsList.get(currentAccountIndex);
        txtCurrentAccountName.setText(info.name);
        txtAccountBalance.setText(String.format(Locale.US, "LKR %.2f", info.balance));
        txtAccountNumber.setText(info.number);
    }
}

    private void showAccountSwitchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Switch Account");
String[] names = new String[accountsList.size()];
for (int i = 0; i < accountsList.size(); i++) {
    names[i] = accountsList.get(i).name;
}
builder.setItems(names, (dialog, which) -> {
    currentAccountIndex = which;
    updateAccountUI();
    Toast.makeText(this, "Switched to " + names[which], Toast.LENGTH_SHORT).show();
});
        builder.show();
    }

    private void setupSavingsWidget() {
        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        View btnUpdateSavings = findViewById(R.id.btnUpdateSavings);
        View cardSavingsWidget = findViewById(R.id.cardSavingsWidget);

        if (txtCurrentSavingsValue != null && btnUpdateSavings != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
            btnUpdateSavings.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            if (cardSavingsWidget != null) {
                cardSavingsWidget.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            }
        }
    }

    private void loadSavingsFromFirestore(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentSavings = documentSnapshot.getString("currentSavings");
                        if (currentSavings != null && !currentSavings.trim().isEmpty()) {
                            try {
                                double amt = Double.parseDouble(currentSavings.trim());
                                txtValue.setText(String.format(Locale.US, "LKR %.2f", amt));
                            } catch (NumberFormatException e) {
                                txtValue.setText("LKR " + currentSavings);
                            }
                        } else {
                            txtValue.setText("LKR 0.00");
                        }
                    }
        });
    }

    private void loadAccountsFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    accountsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        Double balance = doc.getDouble("balance");
                        String number = doc.getString("accountNumber");
                        if (name != null && balance != null && number != null) {
                            accountsList.add(new AccountInfo(name, balance, number));
                        }
                    }
                    if (!accountsList.isEmpty()) {
                        currentAccountIndex = Math.min(currentAccountIndex, accountsList.size() - 1);
                    }
                    updateAccountUI();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load accounts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showUpdateSavingsDialog(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Current Savings");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount (LKR)");

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    double amt = Double.parseDouble(val);
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update("currentSavings", String.valueOf(amt))
                            .addOnSuccessListener(aVoid -> {
                                txtValue.setText(String.format(Locale.US, "LKR %.2f", amt));
                                Toast.makeText(this, "Savings updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        if (txtCurrentSavingsValue != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
        }
    }

    private void setupSecurityButton() {
        View btnSecurity = findViewById(R.id.btnSecurity);
        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> {
                boolean isPinSet = PinHelper.isPinSet(this);
                String[] options;
                if (isPinSet) {
                    options = new String[]{"Change PIN Lock", "Disable PIN Lock"};
                } else {
                    options = new String[]{"Enable PIN Lock"};
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("PIN Lock Security");
                builder.setItems(options, (dialog, which) -> {
                    if (!isPinSet) {
                        Intent intent = new Intent(this, PinSetupActivity.class);
                        startActivity(intent);
                    } else {
                        if (which == 0) {
                            Intent intent = new Intent(this, PinSetupActivity.class);
                            startActivity(intent);
                        } else if (which == 1) {
                            PinHelper.clearPin(this);
                            Toast.makeText(this, "PIN Lock disabled successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }
    }
}