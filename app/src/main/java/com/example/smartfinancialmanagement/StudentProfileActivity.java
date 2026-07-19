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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StudentProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private TextView txtProfileEmail, txtStatGoalsValue, txtStatSavedValue, txtStatScoreValue;
    private ImageView imgProfileAvatar;
    private View btnEditAvatar;
    
    private MaterialCardView menuAchievements, menuFinancialReports, menuAccountSettings, cardSignOut;

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
                imgProfileAvatar.setImageURI(internalUri);
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
        setContentView(R.layout.activity_student_profile);

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
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        
        txtStatGoalsValue = findViewById(R.id.txtStatGoalsValue);
        txtStatSavedValue = findViewById(R.id.txtStatSavedValue);
        txtStatScoreValue = findViewById(R.id.txtStatScoreValue);
        

        menuAchievements = findViewById(R.id.menuAchievements);
        menuFinancialReports = findViewById(R.id.menuFinancialReports);
        menuAccountSettings = findViewById(R.id.menuAccountSettings);
        cardSignOut = findViewById(R.id.cardSignOut);
        
        // Disable inline editing of name as it is now managed elsewhere or read-only
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
            
            // Load Name from Firestore
            db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        etProfileName.setText(documentSnapshot.getString("name"));
                    }
                });

            // Load University and Course from Firestore
            db.collection("users").document(user.getUid()).collection("student_profile").document("profile_data").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String uni = documentSnapshot.getString("university");
                        String course = documentSnapshot.getString("course");
                        
                        TextView txtUni = findViewById(R.id.txtProfileUniversity);
                        if (txtUni != null) {
                            if (uni != null && !uni.isEmpty()) {
                                if (course != null && !course.isEmpty()) {
                                    txtUni.setText(uni + " • " + course);
                                } else {
                                    txtUni.setText(uni);
                                }
                            } else {
                                txtUni.setText("University not set");
                            }
                        }
                    }
                });
        }

        // Load local avatar
        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_AVATAR_URI, null);
        if (uriStr != null) {
            imgProfileAvatar.setImageURI(Uri.parse(uriStr));
        }
    }

    private void saveAvatarUri(String uriStr) {
        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_URI, uriStr).apply();
    }

    private void loadStats() {
        if (user == null) return;

        // Load Savings Stats
        db.collection("users").document(user.getUid()).collection("savings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSaved = 0;
                    int activeGoals = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SavingModel saving = doc.toObject(SavingModel.class);
                        totalSaved += saving.getCurrentAmount();
                        if (!"COMPLETED".equals(saving.getStatus())) {
                            activeGoals++;
                        }
                    }
                    txtStatGoalsValue.setText(String.valueOf(activeGoals));
                    txtStatSavedValue.setText(CurrencyHelper.formatMoney(this, totalSaved));
                    
                    // Simple points system: 1 point per 1000 saved
                    int points = (int) (totalSaved / 1000);
                    txtStatScoreValue.setText(String.valueOf(points) + " Pts");
                });
    }

    private void setupListeners() {
        android.widget.ImageButton btnBackToHome = findViewById(R.id.btnBackToHome);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> finish());
        }

        if (btnEditAvatar != null) btnEditAvatar.setOnClickListener(v -> pickImage());
        if (imgProfileAvatar != null) imgProfileAvatar.setOnClickListener(v -> pickImage());
        

        
        if (menuAchievements != null) {
            menuAchievements.setOnClickListener(v -> startActivity(new Intent(this, SavingsPassportActivity.class)));
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
}
