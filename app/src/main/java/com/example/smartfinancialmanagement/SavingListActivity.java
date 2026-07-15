package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SavingListActivity extends AppCompatActivity {

    private RecyclerView rvSavings;
    private SavingAdapter adapter;
    private List<SavingModel> savingList;
    private List<SavingModel> savingListFiltered;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddSaving;
    private EditText etSearch;
    private ImageView btnBack, btnGenerateReport, btnFilter;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_list);

        initViews();
        setupFirebase();
        setupRecyclerView();
        setupListeners();
        fetchSavingsData();
    }

    private void initViews() {
        rvSavings = findViewById(R.id.rvSavings);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        fabAddSaving = findViewById(R.id.fabAddSaving);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnFilter = findViewById(R.id.btnFilter);
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // For testing if Auth is not active, fallback or handle error
            userId = "test_user";
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Savings").child(userId);
    }

    private void setupRecyclerView() {
        rvSavings.setLayoutManager(new LinearLayoutManager(this));
        savingList = new ArrayList<>();
        savingListFiltered = new ArrayList<>();
        adapter = new SavingAdapter(this, savingListFiltered, new SavingAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(SavingModel savingModel) {
                Intent intent = new Intent(SavingListActivity.this, SavingUpdateGoalActivity.class);
                intent.putExtra("SAVING_ID", savingModel.getSavingId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(SavingModel savingModel) {
                showDeleteDialog(savingModel);
            }

            @Override
            public void onViewClick(SavingModel savingModel) {
                Intent intent = new Intent(SavingListActivity.this, SavingDetailsActivity.class);
                intent.putExtra("SAVING_ID", savingModel.getSavingId());
                startActivity(intent);
            }
        });
        rvSavings.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        fabAddSaving.setOnClickListener(v -> {
            Intent intent = new Intent(SavingListActivity.this, SavingAddGoalActivity.class);
            startActivity(intent);
        });

        btnGenerateReport.setOnClickListener(v -> {
            Intent intent = new Intent(SavingListActivity.this, SavingGenerateReportActivity.class);
            startActivity(intent);
        });

        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Filter options coming soon", Toast.LENGTH_SHORT).show();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchSavingsData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SavingModel saving = dataSnapshot.getValue(SavingModel.class);
                    if (saving != null) {
                        savingList.add(saving);
                    }
                }
                filterList(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavingListActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String query) {
        savingListFiltered.clear();
        if (query.isEmpty()) {
            savingListFiltered.addAll(savingList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (SavingModel saving : savingList) {
                if (saving.getSavingTitle() != null && saving.getSavingTitle().toLowerCase().contains(lowerCaseQuery)) {
                    savingListFiltered.add(saving);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (savingListFiltered.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvSavings.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvSavings.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteDialog(SavingModel savingModel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this saving goal?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSavingGoal(savingModel.getSavingId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSavingGoal(String savingId) {
        databaseReference.child(savingId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SavingListActivity.this, "Saving goal deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SavingListActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
