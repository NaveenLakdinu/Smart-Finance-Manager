package com.example.smartfinancialmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

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

public class MultiAccountProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private TextView txtProfileEmail, txtProfileType, txtStatAccountsValue, txtStatTotalBalanceValue;
    private ImageView imgProfileAvatar;
    private View btnEditAvatar;

    private MaterialCardView menuFinancialReports, menuAccountSettings, cardSignOut;

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
        setContentView(R.layout.activity_multi_account_profile);

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
        txtProfileType = findViewById(R.id.txtProfileType);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);

        txtStatAccountsValue = findViewById(R.id.txtStatAccountsValue);
        txtStatTotalBalanceValue = findViewById(R.id.txtStatTotalBalanceValue);

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

            db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        etProfileName.setText(documentSnapshot.getString("name"));
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
            .collection("accounts").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalAccounts = queryDocumentSnapshots.size();
                double totalBalance = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Double bal = doc.getDouble("balance");
                    if (bal != null) {
                        totalBalance += bal;
                    }
                }

                if (txtStatAccountsValue != null) {
                    txtStatAccountsValue.setText(String.valueOf(totalAccounts));
                }
                
                if (txtStatTotalBalanceValue != null) {
                    txtStatTotalBalanceValue.setText(String.format(Locale.US, "LKR %,.0f", totalBalance));
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
            getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE).edit().remove("avatar_uri").apply();
        }
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
