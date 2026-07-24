package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import android.widget.ScrollView;
import android.widget.ProgressBar;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MultiAccountDashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtCurrentAccountName, txtAccountBalance, txtAccountNumber;
    private LinearLayout btnSwitchAccount;
    private View btnTopLogout;
    private MaterialCardView cardTransfer, cardStatements, cardLoanManager, cardCards, cardAddAccount;
    private MaterialCardView cardSubscriptionManager, cardSavingManager, cardUtilityManager;

    private List<AccountInfo> accountsList = new ArrayList<>();
    private int currentAccountIndex = 0;

    private static class AccountInfo {
        String documentId;
        String name;
        double balance;
        String number;
        AccountInfo(String documentId, String name, double balance, String number) {
            this.documentId = documentId;
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
        updateAccountUI();

        btnSwitchAccount.setOnClickListener(v -> showAccountSwitchDialog());

        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        View btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(notifBtn -> showNotificationPanelDialog());
        }
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
        cardTransfer.setOnClickListener(v -> {
            if (accountsList.size() < 2) {
                Toast.makeText(this, "Add at least 2 accounts to transfer", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MultiAccountDashboardActivity.this, TransferActivity.class);
            ArrayList<String> docIds = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> numbers = new ArrayList<>();
            double[] balances = new double[accountsList.size()];
            for (int i = 0; i < accountsList.size(); i++) {
                AccountInfo a = accountsList.get(i);
                docIds.add(a.documentId);
                names.add(a.name);
                numbers.add(a.number);
                balances[i] = a.balance;
            }
            intent.putStringArrayListExtra("DOC_IDS", docIds);
            intent.putStringArrayListExtra("NAMES", names);
            intent.putStringArrayListExtra("NUMBERS", numbers);
            intent.putExtra("BALANCES", balances);
            intent.putExtra("CURRENT_ACCOUNT_INDEX", currentAccountIndex);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        cardStatements.setOnClickListener(v -> {
            Intent intent = new Intent(MultiAccountDashboardActivity.this, TransferHistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        cardCards.setOnClickListener(v -> Toast.makeText(this, "Card Management - Coming Soon", Toast.LENGTH_SHORT).show());
        cardAddAccount.setOnClickListener(v -> showAddAccountDialog());

        cardLoanManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoanFormActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        cardSubscriptionManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubscriptionManagerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingManagerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        cardUtilityManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, UtilityManagerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        if (hour >= 5 && hour < 12) return "Good Morning";
        else if (hour >= 12 && hour < 17) return "Good Afternoon";
        else if (hour >= 17 && hour < 21) return "Good Evening";
        else return "Good Night";
    }

    private void updateAccountUI() {
        if (accountsList.isEmpty()) {
            txtCurrentAccountName.setText("No Account");
            txtAccountBalance.setText("Rs 0.00");
            txtAccountNumber.setText("----");
        } else {
            AccountInfo info = accountsList.get(currentAccountIndex);
            txtCurrentAccountName.setText(info.name);
            txtAccountBalance.setText(String.format(Locale.US, "Rs %.2f", info.balance));
            txtAccountNumber.setText(info.number);
        }
    }

    private void showAccountSwitchDialog() {
        if (accountsList.isEmpty()) {
            Toast.makeText(this, "No accounts available. Please add a new account.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
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

    private void showAddAccountDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
        builder.setTitle("Add New Account");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);

        EditText editName = new EditText(this);
        editName.setHint("Account Name (e.g. Savings)");
        editName.setBackgroundResource(R.drawable.bg_input_dark);
        editName.setTextColor(android.graphics.Color.WHITE);
        editName.setHintTextColor(0x80FFFFFF);
        editName.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        container.addView(editName, params);

        EditText editNumber = new EditText(this);
        editNumber.setHint("Account Number");
        editNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editNumber.setBackgroundResource(R.drawable.bg_input_dark);
        editNumber.setTextColor(android.graphics.Color.WHITE);
        editNumber.setHintTextColor(0x80FFFFFF);
        editNumber.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        container.addView(editNumber, params);

        EditText editBalance = new EditText(this);
        editBalance.setHint("Initial Balance (LKR)");
        editBalance.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editBalance.setBackgroundResource(R.drawable.bg_input_dark);
        editBalance.setTextColor(android.graphics.Color.WHITE);
        editBalance.setHintTextColor(0x80FFFFFF);
        editBalance.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        container.addView(editBalance, params);

        builder.setView(container);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            String number = editNumber.getText().toString().trim();
            String balanceStr = editBalance.getText().toString().trim();

            if (name.isEmpty() || number.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double balance = Double.parseDouble(balanceStr);

                java.util.Map<String, Object> accountData = new java.util.HashMap<>();
                accountData.put("name", name);
                accountData.put("accountNumber", number);
                accountData.put("balance", balance);

                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .collection("accounts").add(accountData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Account added successfully", Toast.LENGTH_SHORT).show();
                            loadAccountsFromFirestore();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to add account", Toast.LENGTH_SHORT).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid balance amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
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
                                txtValue.setText(String.format(Locale.US, "Rs %.2f", amt));
                            } catch (NumberFormatException e) {
                                txtValue.setText("Rs " + currentSavings);
                            }
                        } else {
                            txtValue.setText("Rs 0.00");
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
                            accountsList.add(new AccountInfo(doc.getId(), name, balance, number));
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
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
                                txtValue.setText(String.format(Locale.US, "Rs %.2f", amt));
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

        NotificationPanelHelper.checkAndShowOnResume(this);
        loadAccountsFromFirestore();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
                builder.setTitle("PIN Lock Security");
                builder.setItems(options, (dialog, which) -> {
                    if (!isPinSet) {
                        Intent intent = new Intent(this, PinSetupActivity.class);
                        startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        if (which == 0) {
                            Intent intent = new Intent(this, PinSetupActivity.class);
                            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    private void animateCards(View... cards) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                cards[i].setAlpha(0f);
                cards[i].setTranslationY(40f);
                final int delay = i * 100;
                cards[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(delay)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
            }
        }
    }

    private void showNotificationPanelDialog() {
        NotificationPanelHelper.show(this);
    }
}

