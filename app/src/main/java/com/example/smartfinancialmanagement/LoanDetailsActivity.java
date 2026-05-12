package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoanDetailsActivity extends AppCompatActivity {

    // UI Variables
    private EditText etLoanAmount, etMonthlyInstallment, etMonthsPaid;
    private Spinner spinnerPaymentMethod;
    private Button btnNext;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_details);

        // 1. XML එකේ ID සමඟ Java Variables සම්බන්ධ කිරීම
        etLoanAmount = findViewById(R.id.etLoanAmount);
        etMonthlyInstallment = findViewById(R.id.etMonthlyInstallment);
        etMonthsPaid = findViewById(R.id.etMonthsPaid);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        btnNext = findViewById(R.id.btnNextLoan);
        backButton = findViewById(R.id.backButton);

        // 2. කලින් දත්ත Singleton එකේ තිබේ නම් ඒවා EditText වලට පිරවීම
        loadExistingData();

        // 3. Back Button එක එබූ විට ක්‍රියාත්මක වන කොටස
        backButton.setOnClickListener(v -> {
            // දත්ත සේව් නොකර යන නිසා Singleton එකේ state එක false කරන්න පුළුවන්
            UserRegistrationData.getInstance().hasLoan = false;
            finish();
        });

        // 4. Next Button එක එබූ විට දත්ත පරීක්ෂා කර සේව් කිරීම
        btnNext.setOnClickListener(v -> {
            saveLoanData();
        });
    }

    private void saveLoanData() {
        // අගයන් ලබා ගැනීම
        String amount = etLoanAmount.getText().toString().trim();
        String installment = etMonthlyInstallment.getText().toString().trim();
        String months = etMonthsPaid.getText().toString().trim();

        // Spinner එකේ දැනට තෝරා ඇති අගය සහ එහි ස්ථානය (Position) ලබා ගැනීම
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();
        int selectedPosition = spinnerPaymentMethod.getSelectedItemPosition();

        // VALIDATION: හිස්තැන් තිබේදැයි පරීක්ෂා කිරීම
        if (amount.isEmpty() || installment.isEmpty() || months.isEmpty()) {
            Toast.makeText(this, "Please fill all loan details", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDATION: Spinner එකේ "Select Payment Method" (0 වෙනි පේළිය) තෝරා ඇත්නම්
        if (selectedPosition == 0) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        // SINGLETON එකට දත්ත ඇතුළත් කිරීම
        UserRegistrationData data = UserRegistrationData.getInstance();
        data.hasLoan = true;
        data.loanAmount = amount;
        data.monthlyInstallment = installment;
        data.monthsPaid = months;
        data.paymentMethod = paymentMethod;

        Toast.makeText(this, "Loan details updated!", Toast.LENGTH_SHORT).show();

        // RegisterActivity එකට ආපසු යෑම
        finish();
    }

    private void loadExistingData() {
        UserRegistrationData data = UserRegistrationData.getInstance();
        // කලින් මේ පිටුවේ දත්ත පුරවා ඇත්නම් ඒවා නැවත පෙන්වීම
        if (data.hasLoan) {
            etLoanAmount.setText(data.loanAmount);
            etMonthlyInstallment.setText(data.monthlyInstallment);
            etMonthsPaid.setText(data.monthsPaid);

            // සටහන: Spinner එකේ අගය නැවත පෙන්වීමට නම් ArrayAdapter එකේ
            // setSelection පාවිච්චි කළ යුතුය.
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Android Device එකේ Back Button එක එබූ විට
        UserRegistrationData.getInstance().hasLoan = false;
    }
}