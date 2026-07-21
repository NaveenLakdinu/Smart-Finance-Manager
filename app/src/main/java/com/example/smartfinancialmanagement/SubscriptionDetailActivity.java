package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.Locale;

public class SubscriptionDetailActivity extends AppCompatActivity {

    private TextView txtDetailLogo, txtDetailName, txtDetailAmount, txtDetailStatus;
    private TextView txtDetailPaymentDay, txtDetailBillingCycle, txtDetailRenewDate;
    private MaterialButton btnMarkPaid, btnEdit, btnDelete;
    private ImageView backButton;

    private String docId;
    private Subscription currentSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_detail);

        docId = getIntent().getStringExtra("DOC_ID");
        if (docId == null) {
            finish();
            return;
        }

        initViews();
        loadSubscription();
    }

    private void initViews() {
        txtDetailLogo = findViewById(R.id.txtDetailLogo);
        txtDetailName = findViewById(R.id.txtDetailName);
        txtDetailAmount = findViewById(R.id.txtDetailAmount);
        txtDetailStatus = findViewById(R.id.txtDetailStatus);
        txtDetailPaymentDay = findViewById(R.id.txtDetailPaymentDay);
        txtDetailBillingCycle = findViewById(R.id.txtDetailBillingCycle);
        txtDetailRenewDate = findViewById(R.id.txtDetailRenewDate);
        btnMarkPaid = findViewById(R.id.btnMarkPaid);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());
        btnMarkPaid.setOnClickListener(v -> markAsPaid());
        btnEdit.setOnClickListener(v -> editSubscription());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadSubscription() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions").document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentSubscription = doc.toObject(Subscription.class);
                        currentSubscription.setDocumentId(doc.getId());
                        displaySubscription(currentSubscription);
                    } else {
                        Toast.makeText(this, "Subscription not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displaySubscription(Subscription sub) {
        txtDetailName.setText(sub.getName());
        txtDetailAmount.setText(String.format(Locale.US, "Rs %,.2f", sub.getAmount()));

        String status = sub.getStatus() != null ? sub.getStatus() : "Active";
        txtDetailStatus.setText(status);
        setStatusColor(txtDetailStatus, status);

        txtDetailPaymentDay.setText("Day " + sub.getPaymentDay() + " of each month");
        txtDetailBillingCycle.setText(sub.getBillingCycle());
        txtDetailRenewDate.setText(sub.getRenewDate());

        setLogo(txtDetailLogo, sub.getLogoType(), sub.getName());
    }

    private void markAsPaid() {
        if (currentSubscription == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);
        int nextMonth = currentMonth + 1;
        int nextYear = currentYear;
        if (nextMonth > 11) {
            nextMonth = 0;
            nextYear++;
        }

        cal.set(nextYear, nextMonth, currentSubscription.getPaymentDay());
        String newRenewDate = String.format("%02d/%02d/%d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        currentSubscription.setRenewDate(newRenewDate);

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions").document(docId)
                .update("renewDate", newRenewDate)
                .addOnSuccessListener(v -> {
                    SubscriptionNotificationScheduler.scheduleAll(this, user.getUid(), docId, currentSubscription);
                    txtDetailRenewDate.setText(newRenewDate);
                    Toast.makeText(this, "Marked as paid. Next renewal: " + newRenewDate, Toast.LENGTH_SHORT).show();
                });
    }

    private void editSubscription() {
        Intent intent = new Intent(this, AddSubscriptionActivity.class);
        intent.putExtra("EDIT_DOC_ID", docId);
        startActivity(intent);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setTitle("Delete Subscription")
                .setMessage("Are you sure you want to delete \"" + currentSubscription.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSubscription())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSubscription() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SubscriptionNotificationScheduler.cancelAll(this, user.getUid(), docId);

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions").document(docId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Subscription deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setLogo(TextView logo, String logoType, String name) {
        if (logoType == null) logoType = "";
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16);

        switch (logoType.toLowerCase()) {
            case "netflix":
                logo.setText("N");
                logo.setTextColor(Color.parseColor("#E50914"));
                bg.setColor(Color.parseColor("#000000"));
                break;
            case "spotify":
                logo.setText("S");
                logo.setTextColor(Color.parseColor("#000000"));
                bg.setColor(Color.parseColor("#1DB954"));
                bg.setCornerRadius(50);
                break;
            case "youtube":
                logo.setText("▶");
                logo.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setColor(Color.parseColor("#FF0000"));
                break;
            case "chatgpt":
                logo.setText("AI");
                logo.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setColor(Color.parseColor("#000000"));
                break;
            default:
                if (name != null && name.length() > 0) {
                    logo.setText(String.valueOf(name.charAt(0)).toUpperCase());
                } else {
                    logo.setText("S");
                }
                logo.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setColor(Color.parseColor("#071A33"));
                break;
        }
        logo.setBackground(bg);
    }

    private void setStatusColor(TextView statusView, String status) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E6E8EB"));
        bg.setCornerRadius(50);
        statusView.setBackground(bg);

        if (status.equalsIgnoreCase("Active")) {
            statusView.setTextColor(Color.parseColor("#2EAD4A"));
        } else if (status.equalsIgnoreCase("Upcoming")) {
            statusView.setTextColor(Color.parseColor("#FF9800"));
        } else if (status.equalsIgnoreCase("Expired")) {
            statusView.setTextColor(Color.parseColor("#F44336"));
        } else {
            statusView.setTextColor(Color.parseColor("#607D8B"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (docId != null) loadSubscription();
    }
}
