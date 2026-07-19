package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class AddSubscriptionActivity extends AppCompatActivity {

    private EditText editPlanName, editMonthlyCost;
    private Spinner spinnerPaymentDay, spinnerLogoType;
    private RadioGroup radioBillingCycle;
    private MaterialButton btnSaveSubscription;
    private ImageView backButton;

    private String editDocumentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscription);

        initViews();
        setupSpinners();
        setupListeners();

        editDocumentId = getIntent().getStringExtra("EDIT_DOC_ID");
        if (editDocumentId != null) {
            loadSubscriptionForEdit();
        }
    }

    private void initViews() {
        editPlanName = findViewById(R.id.editPlanName);
        editMonthlyCost = findViewById(R.id.editMonthlyCost);
        spinnerPaymentDay = findViewById(R.id.spinnerPaymentDay);
        spinnerLogoType = findViewById(R.id.spinnerLogoType);
        radioBillingCycle = findViewById(R.id.radioBillingCycle);
        btnSaveSubscription = findViewById(R.id.btnSaveSubscription);
        backButton = findViewById(R.id.backButton);
    }

    private void setupSpinners() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentDay.setAdapter(dayAdapter);

        String[] services = {"Netflix", "Spotify", "YouTube", "ChatGPT", "Other"};
        ArrayAdapter<String> logoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, services);
        logoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLogoType.setAdapter(logoAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        btnSaveSubscription.setOnClickListener(v -> saveSubscription());
    }

    private void saveSubscription() {
        String name = editPlanName.getText().toString().trim();
        String costStr = editMonthlyCost.getText().toString().trim();

        if (name.isEmpty()) {
            editPlanName.setError("Please enter plan name");
            editPlanName.requestFocus();
            return;
        }
        if (costStr.isEmpty()) {
            editMonthlyCost.setError("Please enter monthly cost");
            editMonthlyCost.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(costStr);
        } catch (NumberFormatException e) {
            editMonthlyCost.setError("Invalid amount");
            editMonthlyCost.requestFocus();
            return;
        }

        int paymentDay = spinnerPaymentDay.getSelectedItemPosition() + 1;
        String billingCycle = ((RadioButton) findViewById(radioBillingCycle.getCheckedRadioButtonId()))
                .getText().toString();
        String logoType = spinnerLogoType.getSelectedItem().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, paymentDay);
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.MONTH, 1);
        }
        String renewDate = String.format("%02d/%02d/%d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        Subscription sub = new Subscription(name, renewDate, "Active", logoType,
                amount, paymentDay, billingCycle, System.currentTimeMillis());

        if (editDocumentId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(user.getUid())
                    .collection("subscriptions").document(editDocumentId)
                    .update(sub.toMap())
                    .addOnSuccessListener(v -> {
                        SubscriptionNotificationScheduler.scheduleAll(this, user.getUid(), editDocumentId, sub);
                        Toast.makeText(this, "Subscription updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            FirebaseFirestore.getInstance()
                    .collection("users").document(user.getUid())
                    .collection("subscriptions")
                    .add(sub.toMap())
                    .addOnSuccessListener(docRef -> {
                        SubscriptionNotificationScheduler.scheduleAll(this, user.getUid(), docRef.getId(), sub);
                        Toast.makeText(this, "Subscription saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadSubscriptionForEdit() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions").document(editDocumentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editPlanName.setText(doc.getString("name"));
                        editMonthlyCost.setText(String.valueOf(doc.getDouble("amount")));

                        Long payDay = doc.getLong("paymentDay");
                        if (payDay != null && payDay > 0 && payDay <= 31) {
                            spinnerPaymentDay.setSelection((int)(payDay - 1));
                        }

                        String cycle = doc.getString("billingCycle");
                        if ("Yearly".equals(cycle)) {
                            radioBillingCycle.check(R.id.radioYearly);
                        }

                        String logo = doc.getString("logoType");
                        if (logo != null) {
                            String[] services = {"Netflix", "Spotify", "YouTube", "ChatGPT", "Other"};
                            for (int i = 0; i < services.length; i++) {
                                if (services[i].equalsIgnoreCase(logo)) {
                                    spinnerLogoType.setSelection(i);
                                    break;
                                }
                            }
                        }
                        btnSaveSubscription.setText("Update Subscription");
                    }
                });
    }
}
