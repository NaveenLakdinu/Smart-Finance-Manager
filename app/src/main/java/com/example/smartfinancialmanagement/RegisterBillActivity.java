package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterBillActivity extends AppCompatActivity {

    // Declare layout elements
    private ImageView backButton;
    private EditText editBillName;
    private EditText editAccountNo;
    private EditText editPlace;
    private EditText editPaymentDate;
    private Spinner spinnerServiceProvider;
    private Button btnRegisterSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bill); // Connects to your XML layout

        // 1. Initialize Views from XML
        backButton = findViewById(R.id.backButton);
        editBillName = findViewById(R.id.editBillName);
        editAccountNo = findViewById(R.id.editAccountNo);
        editPlace = findViewById(R.id.editPlace);
        editPaymentDate = findViewById(R.id.editPaymentDate);
        spinnerServiceProvider = findViewById(R.id.spinnerServiceProvider);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);

        // 2. Populate Service Provider Dropdown (Spinner)
        List<String> providersList = new ArrayList<>();
        providersList.add("Select Provider"); // Placeholder row
        providersList.add("Electricity");
        providersList.add("Water");
        providersList.add("Telephone");
        providersList.add("Internet");
        providersList.add("Television");
        providersList.add("Rent");

        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                providersList
        );
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceProvider.setAdapter(providerAdapter);


        // 3. Set Up Interactive Date Picker Calendar Dialog
        editPaymentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current day's date to initialize the calendar picker view
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Show DatePickerDialog window view
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        RegisterBillActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                // Formats the selected date sequence and sets it into the entry frame
                                String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                editPaymentDate.setText(formattedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });


        // 4. Handle Back Arrow Navigation Action Frame
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this registration form view and goes back to previous screen
            }
        });


        // 5. Handle Form Inputs Submission Validation Setup Block
        btnRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Read text string data out from inputs
                String billName = editBillName.getText().toString().trim();
                String accountNo = editAccountNo.getText().toString().trim();
                String place = editPlace.getText().toString().trim();
                String paymentDate = editPaymentDate.getText().toString().trim();
                int providerPosition = spinnerServiceProvider.getSelectedItemPosition();

                // Validation checking conditions
                if (billName.isEmpty()) {
                    Toast.makeText(RegisterBillActivity.this, "Please enter Bill Name", Toast.LENGTH_SHORT).show();
                } else if (accountNo.isEmpty()) {
                    Toast.makeText(RegisterBillActivity.this, "Please enter Account Number", Toast.LENGTH_SHORT).show();
                } else if (place.isEmpty()) {
                    Toast.makeText(RegisterBillActivity.this, "Please enter Place location", Toast.LENGTH_SHORT).show();
                } else if (paymentDate.isEmpty()) {
                    Toast.makeText(RegisterBillActivity.this, "Please select a Payment Date", Toast.LENGTH_SHORT).show();
                } else if (providerPosition == 0) {
                    Toast.makeText(RegisterBillActivity.this, "Please select a Service Provider", Toast.LENGTH_SHORT).show();
                } else {
                    // Success Scenario
                    String chosenProvider = spinnerServiceProvider.getSelectedItem().toString();
                    Toast.makeText(RegisterBillActivity.this, "Successfully Registered: " + billName, Toast.LENGTH_LONG).show();

                    // Closes screen back to home dashboard after saving successfully
                    finish();
                }
            }
        });
    }
}