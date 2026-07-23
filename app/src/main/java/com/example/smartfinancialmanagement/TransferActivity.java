package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransferActivity extends AppCompatActivity {

    private TextView txtSourceName, txtSourceBalance, txtSourceNumber;
    private TextView txtDestName, txtDestBalance, txtDestNumber;
    private TextView txtAvailable;
    private EditText editAmount, editNote;
    private MaterialButton btnTransfer;

    private ArrayList<String> docIds, names, numbers;
    private ArrayList<Double> balances;

    private int sourceIndex = -1;
    private int destIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        initViews();
        parseIntentData();
        setupAccountPickers();
        setupTransferButton();
    }

    private void initViews() {
        txtSourceName = findViewById(R.id.txtSourceName);
        txtSourceBalance = findViewById(R.id.txtSourceBalance);
        txtSourceNumber = findViewById(R.id.txtSourceNumber);
        txtDestName = findViewById(R.id.txtDestName);
        txtDestBalance = findViewById(R.id.txtDestBalance);
        txtDestNumber = findViewById(R.id.txtDestNumber);
        txtAvailable = findViewById(R.id.txtAvailable);
        editAmount = findViewById(R.id.editAmount);
        editNote = findViewById(R.id.editNote);
        btnTransfer = findViewById(R.id.btnTransfer);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void parseIntentData() {
        docIds = getIntent().getStringArrayListExtra("DOC_IDS");
        names = getIntent().getStringArrayListExtra("NAMES");
        numbers = getIntent().getStringArrayListExtra("NUMBERS");

        double[] balArray = getIntent().getDoubleArrayExtra("BALANCES");
        balances = new ArrayList<>();
        if (balArray != null) {
            for (double b : balArray) {
                balances.add(b);
            }
        }

        if (docIds == null || names == null || docIds.isEmpty()) {
            Toast.makeText(this, "No accounts available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sourceIndex = getIntent().getIntExtra("CURRENT_ACCOUNT_INDEX", 0);
        if (sourceIndex < 0 || sourceIndex >= docIds.size()) sourceIndex = 0;
        // Pick first different account as destination
        destIndex = -1;
        for (int i = 0; i < docIds.size(); i++) {
            if (i != sourceIndex) { destIndex = i; break; }
        }
        updateSourceUI();
        updateDestUI();
    }

    private void setupAccountPickers() {
        View cardSource = findViewById(R.id.cardSourceAccount);
        View cardDest = findViewById(R.id.cardDestAccount);

        // Source account is locked to the currently selected account
        cardSource.setOnClickListener(null);
        cardSource.setAlpha(0.7f);

        cardDest.setOnClickListener(v -> showAccountPickerDialog(false));
    }

    private void showAccountPickerDialog(boolean isSource) {
        String[] nameArray = new String[names.size()];
        for (int i = 0; i < names.size(); i++) {
            nameArray[i] = names.get(i) + " - " + String.format(Locale.US, "Rs %.2f", balances.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
        builder.setTitle(isSource ? "Select Source Account" : "Select Destination Account");
        builder.setItems(nameArray, (dialog, which) -> {
            if (isSource) {
                sourceIndex = which;
                updateSourceUI();
            } else {
                destIndex = which;
                updateDestUI();
            }
            updateAvailableText();
        });
        builder.show();
    }

    private void updateSourceUI() {
        if (sourceIndex < 0 || sourceIndex >= names.size()) return;
        txtSourceName.setText(names.get(sourceIndex));
        txtSourceBalance.setText(String.format(Locale.US, "Rs %.2f", balances.get(sourceIndex)));
        txtSourceNumber.setText(numbers.get(sourceIndex));
        updateAvailableText();
    }

    private void updateDestUI() {
        if (destIndex < 0 || destIndex >= names.size()) return;
        txtDestName.setText(names.get(destIndex));
        txtDestBalance.setText(String.format(Locale.US, "Rs %.2f", balances.get(destIndex)));
        txtDestNumber.setText(numbers.get(destIndex));
    }

    private void updateAvailableText() {
        if (sourceIndex >= 0 && sourceIndex < balances.size()) {
            txtAvailable.setText(String.format(Locale.US, "Available: Rs %.2f", balances.get(sourceIndex)));
        }
    }

    private void setupTransferButton() {
        btnTransfer.setOnClickListener(v -> performTransfer());
    }

    private void performTransfer() {
        if (sourceIndex < 0 || destIndex < 0) {
            Toast.makeText(this, "Please select both accounts", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sourceIndex == destIndex) {
            Toast.makeText(this, "Cannot transfer to the same account", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = editAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > balances.get(sourceIndex)) {
            Toast.makeText(this, "Insufficient funds", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog before executing the transfer
        showConfirmationDialog(amount);
    }

    private void showConfirmationDialog(double amount) {
        String fromName = names.get(sourceIndex);
        String toName = names.get(destIndex);
        String note = editNote.getText().toString().trim();
        String amountFormatted = String.format(Locale.US, "Rs %.2f", amount);

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transfer_confirm, null);

        TextView txtDialogFrom = dialogView.findViewById(R.id.txtDialogFrom);
        TextView txtDialogTo = dialogView.findViewById(R.id.txtDialogTo);
        TextView txtDialogAmount = dialogView.findViewById(R.id.txtDialogAmount);
        TextView txtDialogNote = dialogView.findViewById(R.id.txtDialogNote);
        TextView txtDialogNoteLabel = dialogView.findViewById(R.id.txtDialogNoteLabel);

        txtDialogFrom.setText(fromName);
        txtDialogTo.setText(toName);
        txtDialogAmount.setText(amountFormatted);

        if (!note.isEmpty()) {
            txtDialogNote.setText(note);
            txtDialogNoteLabel.setVisibility(View.VISIBLE);
            txtDialogNote.setVisibility(View.VISIBLE);
        } else {
            txtDialogNoteLabel.setVisibility(View.GONE);
            txtDialogNote.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setView(dialogView)
                .setPositiveButton("Confirm Transfer", (d, which) -> executeTransfer(amount))
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    private void executeTransfer(double amount) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        btnTransfer.setEnabled(false);
        btnTransfer.setText("Transferring...");

        String uid = user.getUid();
        DocumentReference sourceRef = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("accounts").document(docIds.get(sourceIndex));
        DocumentReference destRef = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("accounts").document(docIds.get(destIndex));

        final double transferAmount = amount;
        final String note = editNote.getText().toString().trim();
        final String fromName = names.get(sourceIndex);
        final String toName = names.get(destIndex);

        FirebaseFirestore.getInstance().runTransaction((Transaction.Function<Void>) transaction -> {
            com.google.firebase.firestore.DocumentSnapshot sourceDoc = transaction.get(sourceRef);
            com.google.firebase.firestore.DocumentSnapshot destDoc = transaction.get(destRef);

            double sourceBalance = sourceDoc.getDouble("balance");
            double destBalance = destDoc.getDouble("balance");

            if (sourceBalance < transferAmount) {
                throw new FirebaseFirestoreException("Insufficient funds",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }

            transaction.update(sourceRef, "balance", sourceBalance - transferAmount);
            transaction.update(destRef, "balance", destBalance + transferAmount);

            return null;
        }).addOnSuccessListener(aVoid -> {
            logTransfer(uid, fromName, toName, transferAmount, note);
            Toast.makeText(this, "Transfer successful!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnTransfer.setEnabled(true);
            btnTransfer.setText("Transfer");
            Toast.makeText(this, "Transfer failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void logTransfer(String uid, String from, String to, double amount, String note) {
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("fromAccount", from);
        transfer.put("toAccount", to);
        transfer.put("amount", amount);
        transfer.put("note", note);
        transfer.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("transfers")
                .add(transfer);
    }
}
