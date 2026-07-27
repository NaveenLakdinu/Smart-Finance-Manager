package com.example.smartfinancialmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class WorkerProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private TextView txtProfileEmail, txtProfileCompany, txtStatSalaryValue, txtStatBonusValue, txtStatScoreValue;
    private ImageView imgProfileAvatar;
    private View btnEditAvatar;

    private MaterialCardView menuEditIncome, menuPayslips, menuAccountSettings, cardSignOut;

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
        setContentView(R.layout.activity_worker_profile);

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
        txtProfileCompany = findViewById(R.id.txtProfileCompany);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);

        txtStatSalaryValue = findViewById(R.id.txtStatSalaryValue);
        txtStatBonusValue = findViewById(R.id.txtStatBonusValue);
        txtStatScoreValue = findViewById(R.id.txtStatScoreValue);

        menuEditIncome = findViewById(R.id.menuEditIncome);
        menuPayslips = findViewById(R.id.menuPayslips);
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

            db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        etProfileName.setText(documentSnapshot.getString("name"));
                    }
                });

            db.collection("users").document(user.getUid())
                .collection("worker_profile").document("profile_data").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String company = documentSnapshot.getString("companyName");
                        String designation = documentSnapshot.getString("designation");

                        TextView txtCompany = findViewById(R.id.txtProfileCompany);
                        if (txtCompany != null) {
                            if (company != null && !company.isEmpty()) {
                                if (designation != null && !designation.isEmpty()) {
                                    txtCompany.setText(company + " - " + designation);
                                } else {
                                    txtCompany.setText(company);
                                }
                            } else {
                                txtCompany.setText("Company not set");
                            }
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

    private void loadStats() {
        if (user == null) return;

        db.collection("users").document(user.getUid())
            .collection("worker_profile").document("profile_data").get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double salary = documentSnapshot.getDouble("monthlySalary");
                    Double bonus = documentSnapshot.getDouble("bonusAmount");

                    if (salary != null && salary > 0) {
                        txtStatSalaryValue.setText(String.format(Locale.US, "LKR %,.0f", salary));
                    } else {
                        txtStatSalaryValue.setText("LKR 0");
                    }

                    if (bonus != null && bonus > 0) {
                        txtStatBonusValue.setText(String.format(Locale.US, "LKR %,.0f", bonus));
                    } else {
                        txtStatBonusValue.setText("LKR 0");
                    }

                    int points = (int) ((salary != null ? salary : 0) / 1000);
                    txtStatScoreValue.setText(points + " Pts");
                }
            });
    }

    private void setupListeners() {
        ImageButton btnBackToHome = findViewById(R.id.btnBackToHome);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> finish());
        }

        if (btnEditAvatar != null) btnEditAvatar.setOnClickListener(v -> pickImage());
        if (imgProfileAvatar != null) imgProfileAvatar.setOnClickListener(v -> pickImage());

        if (menuEditIncome != null) {
            menuEditIncome.setOnClickListener(v -> startActivity(new Intent(this, WorkerIncomeActivity.class)));
        }

        if (menuPayslips != null) {
            menuPayslips.setOnClickListener(v -> startActivity(new Intent(this, WorkerPayslipActivity.class)));
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
        imgProfileAvatar.setImageURI(uri);
        imgProfileAvatar.setImageTintList(null);
        android.view.ViewGroup.LayoutParams params = imgProfileAvatar.getLayoutParams();
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        imgProfileAvatar.setLayoutParams(params);
        imgProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

        android.view.View parent = (android.view.View) imgProfileAvatar.getParent();
        if (parent != null) {
            parent.setClipToOutline(true);
        }
    }
}
