package com.example.smartfinancialmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BusinessProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private TextView txtProfileEmail, txtProfileBusiness, txtStatRevenueValue, txtStatExpensesValue, txtStatProfitValue;
    private ImageView imgProfileAvatar;
    private View btnEditAvatar;

    private MaterialCardView menuAddBusiness, menuInvoices, menuFinancialReports, menuAccountSettings, cardSignOut;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private static final String PREF_PROFILE = "ProfilePrefs";
    private static final String KEY_AVATAR_URI = "avatar_uri";

    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        saveImageToInternalStorage(selectedImageUri);
                    }
                }
            }
    );

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                File file = new File(getFilesDir(), "profile_avatar.jpg");
                OutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();

                Uri internalUri = Uri.fromFile(file);
                updateAvatarImage(internalUri);
                saveAvatarUri(internalUri.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        initViews();
        loadUserData();
        loadStats();
        setupListeners();
    }

    private void initViews() {
        etProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfileBusiness = findViewById(R.id.txtProfileBusiness);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);

        txtStatRevenueValue = findViewById(R.id.txtStatRevenueValue);
        txtStatExpensesValue = findViewById(R.id.txtStatExpensesValue);
        txtStatProfitValue = findViewById(R.id.txtStatProfitValue);

        menuAddBusiness = findViewById(R.id.menuAddBusiness);
        menuInvoices = findViewById(R.id.menuInvoices);
        menuFinancialReports = findViewById(R.id.menuFinancialReports);
        menuAccountSettings = findViewById(R.id.menuAccountSettings);
        cardSignOut = findViewById(R.id.cardSignOut);

        if (etProfileName != null) {
            etProfileName.setFocusable(false);
            etProfileName.setClickable(false);
            etProfileName.setCursorVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }

    private void loadUserData() {
        if (user != null) {
            txtProfileEmail.setText(user.getEmail());

            // Fetch User Profile Name
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                            etProfileName.setText(documentSnapshot.getString("name"));
                        }
                    });

            // Fetch ALL Businesses for this user
            db.collection("businesses")
                    .whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        TextView txtBiz = findViewById(R.id.txtProfileBusiness);
                        if (txtBiz != null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                StringBuilder bizBuilder = new StringBuilder();
                                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                    String bizName = doc.getString("businessName");
                                    String category = doc.getString("businessCategory");

                                    if (bizName != null && !bizName.isEmpty()) {
                                        if (bizBuilder.length() > 0) {
                                            bizBuilder.append(", ");
                                        }
                                        if (category != null && !category.isEmpty()) {
                                            bizBuilder.append(bizName).append(" (").append(category).append(")");
                                        } else {
                                            bizBuilder.append(bizName);
                                        }
                                    }
                                }
                                txtBiz.setText(bizBuilder.length() > 0 ? bizBuilder.toString() : "Business not set");
                            } else {
                                txtBiz.setText("Business not set");
                            }
                        }
                    });
        }

        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_AVATAR_URI, null);
        if (uriStr != null) {
            updateAvatarImage(Uri.parse(uriStr));
        }
    }

    private void saveAvatarUri(String uriStr) {
        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_URI, uriStr).apply();
    }

    // Fetch CURRENT MONTH stats only & Apply Currency Preference
    private void loadStats() {
        if (user == null) return;

        // Clean single-assignment initialization to keep currencySymbol effectively final
        String rawSymbol = CurrencyHelper.getCurrencySymbol(this);
        final String currencySymbol = (rawSymbol != null && !rawSymbol.trim().isEmpty()) ? rawSymbol : "LKR";

        // Current month token format "yyyy-MM" (e.g. "2026-03")
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        final String currentMonthToken = monthFormat.format(Calendar.getInstance().getTime());

        // Step 1: Query current month revenues
        db.collection("revenues")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(revenueSnapshots -> {
                    double totalRevenue = 0.0;
                    for (QueryDocumentSnapshot doc : revenueSnapshots) {
                        Double amount = doc.getDouble("amount");
                        String dateStr = doc.getString("date");

                        // Filter strictly for current month
                        if (amount != null && dateStr != null && dateStr.startsWith(currentMonthToken)) {
                            totalRevenue += amount;
                        }
                    }

                    final double finalRevenue = totalRevenue;

                    // Step 2: Query current month expenses
                    db.collection("expenses")
                            .whereEqualTo("userId", user.getUid())
                            .get()
                            .addOnSuccessListener(expenseSnapshots -> {
                                double totalExpenses = 0.0;
                                for (QueryDocumentSnapshot doc : expenseSnapshots) {
                                    Double amount = doc.getDouble("amount");
                                    String dateStr = doc.getString("date");

                                    // Filter strictly for current month
                                    if (amount != null && dateStr != null && dateStr.startsWith(currentMonthToken)) {
                                        totalExpenses += amount;
                                    }
                                }

                                // Apply dynamic currency symbol from CurrencyHelper
                                txtStatRevenueValue.setText(String.format(Locale.US, "%s %,.0f", currencySymbol, finalRevenue));
                                txtStatExpensesValue.setText(String.format(Locale.US, "%s %,.0f", currencySymbol, totalExpenses));

                                // Calculate Net Profit & Profit Margin (Current Month)
                                double netProfit = finalRevenue - totalExpenses;
                                double profitMarginPercent = 0.0;

                                if (finalRevenue > 0) {
                                    profitMarginPercent = (netProfit / finalRevenue) * 100;
                                }

                                txtStatProfitValue.setText(String.format(Locale.US, "%.1f%%", profitMarginPercent));
                            });
                });
    }

    private void setupListeners() {
        ImageButton btnBackToHome = findViewById(R.id.btnBackToHome);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> finish());
        }

        if (btnEditAvatar != null) btnEditAvatar.setOnClickListener(v -> pickImage());
        if (imgProfileAvatar != null) imgProfileAvatar.setOnClickListener(v -> pickImage());

        if (menuAddBusiness != null) {
            menuAddBusiness.setOnClickListener(v -> startActivity(new Intent(this, AddBusinessActivity.class)));
        }

        if (menuInvoices != null) {
            menuInvoices.setOnClickListener(v -> startActivity(new Intent(this, InvoiceHubActivity.class)));
        }

        if (menuFinancialReports != null) {
            menuFinancialReports.setOnClickListener(v -> startActivity(new Intent(this, FinancialReportsActivity.class)));
        }

        if (menuAccountSettings != null) {
            menuAccountSettings.setOnClickListener(v -> startActivity(new Intent(this, AccountSettingsActivity.class)));
        }

        if (cardSignOut != null) {
            cardSignOut.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(this, LoginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateAvatarImage(Uri uri) {
        try {
            imgProfileAvatar.setImageURI(uri);
        } catch (SecurityException e) {
            getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE).edit().remove("avatar_uri").apply();
        }
        imgProfileAvatar.setImageTintList(null);
        android.view.ViewGroup.LayoutParams params = imgProfileAvatar.getLayoutParams();
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        imgProfileAvatar.setLayoutParams(params);
        imgProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

        View parent = (View) imgProfileAvatar.getParent();
        if (parent != null) {
            parent.setClipToOutline(true);
        }
    }
}