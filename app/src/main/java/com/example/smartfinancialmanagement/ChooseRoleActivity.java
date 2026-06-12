package com.example.smartfinancialmanagement;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ChooseRoleActivity extends AppCompatActivity {

    // Role card views
    LinearLayout roleStudent, roleWorker, roleBusiness, roleMulti;
    // Radio icons
    ImageView radioStudent, radioWorker, radioBusinessOwner, radioMulti;
    // Continue button
    MaterialButton continueBtn;

    String selectedRole = null;

    // Role card IDs mapped
    LinearLayout[] allCards;
    ImageView[] allRadios;
    String[] roleKeys = {"student", "worker", "business", "multi"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        // Bind views
        roleStudent       = findViewById(R.id.roleStudent);
        roleWorker        = findViewById(R.id.roleWorker);
        roleBusiness      = findViewById(R.id.roleBusiness);
        roleMulti         = findViewById(R.id.roleMulti);

        radioStudent      = findViewById(R.id.radioStudent);
        radioWorker       = findViewById(R.id.radioWorker);
        radioBusinessOwner= findViewById(R.id.radioBusinessOwner);
        radioMulti        = findViewById(R.id.radioMulti);

        continueBtn       = findViewById(R.id.continueBtn);

        allCards  = new LinearLayout[]{roleStudent, roleWorker, roleBusiness, roleMulti};
        allRadios = new ImageView[]{radioStudent, radioWorker, radioBusinessOwner, radioMulti};

        // Back
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Card click listeners
        for (int i = 0; i < allCards.length; i++) {
            final int idx = i;
            allCards[i].setOnClickListener(v -> selectRole(idx));
        }

        // Continue
        continueBtn.setOnClickListener(v -> {
            if (selectedRole == null) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save role to SharedPreferences
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .putString("user_role", selectedRole)
                    .apply();

            // Navigate to next page (RegisterActivity)
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void selectRole(int idx) {
        // Reset all cards
        for (int i = 0; i < allCards.length; i++) {
            allCards[i].setBackgroundResource(R.drawable.role_card_normal);
            allRadios[i].setImageResource(R.drawable.ic_radio_unchecked);
        }

        // Highlight selected
        allCards[idx].setBackgroundResource(R.drawable.role_card_selected);
        allRadios[idx].setImageResource(R.drawable.ic_radio_checked);

        selectedRole = roleKeys[idx];
        continueBtn.setEnabled(true);
        continueBtn.setAlpha(1.0f);
    }
}
