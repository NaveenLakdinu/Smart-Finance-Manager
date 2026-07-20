package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkerTasksActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnClearSearch;
    private TextView filterAll, filterHigh, filterMedium, filterLow;
    private String currentFilter = "All";

    private TextView tvStatTotal, tvStatActive, tvStatDone, tvStatOverdue, tvOverallPercent;
    private android.widget.ProgressBar overallProgress;

    private RecyclerView recyclerTasks;
    private View emptyState;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_tasks);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        initViews();
        setupBackPressed();
        setupRecyclerView();
        setupSearchAndClear();
        setupFilters();
        setupAddButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromFirestore();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        filterAll = findViewById(R.id.filterAll);
        filterHigh = findViewById(R.id.filterHigh);
        filterMedium = findViewById(R.id.filterMedium);
        filterLow = findViewById(R.id.filterLow);

        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatActive = findViewById(R.id.tvStatActive);
        tvStatDone = findViewById(R.id.tvStatDone);
        tvStatOverdue = findViewById(R.id.tvStatOverdue);
        tvOverallPercent = findViewById(R.id.tvOverallPercent);
        overallProgress = findViewById(R.id.overallProgress);

        recyclerTasks = findViewById(R.id.recyclerTasks);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(filteredTasks, (task, isChecked) -> {
            String newStatus = isChecked ? "Completed" : "In Progress";
            int newProgress = isChecked ? 100 : task.getProgress();
            if (!isChecked && newProgress == 0) newProgress = 10;

            db.collection("users").document(uid)
                    .collection("tasks").document(task.getId())
                    .update("status", newStatus, "progress", newProgress)
                    .addOnSuccessListener(aVoid -> {
                        task.setCompleted(isChecked);
                        updateStats();
                        applyFiltersAndSearch();
                    });
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(taskAdapter);
    }

    private void setupSearchAndClear() {
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            applyFiltersAndSearch();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFiltersAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        View.OnClickListener filterClickListener = v -> {
            TextView selected = (TextView) v;
            currentFilter = selected.getText().toString().split(" ")[0];

            filterAll.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_glass_card));
            filterAll.setTextColor(ContextCompat.getColor(this, R.color.text_on_dark_primary));
            filterHigh.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_glass_card));
            filterMedium.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_glass_card));
            filterLow.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_glass_card));

            selected.setBackground(ContextCompat.getDrawable(this, R.drawable.tag_yellow_active));
            selected.setTextColor(ContextCompat.getColor(this, R.color.black));

            applyFiltersAndSearch();
        };

        filterAll.setOnClickListener(filterClickListener);
        filterHigh.setOnClickListener(filterClickListener);
        filterMedium.setOnClickListener(filterClickListener);
        filterLow.setOnClickListener(filterClickListener);
    }

    private void setupAddButton() {
        findViewById(R.id.btnAdd).setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etDescription = dialogView.findViewById(R.id.etTaskDescription);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        TextView tvDueDate = dialogView.findViewById(R.id.tvDueDate);

        String[] priorities = {"High", "Medium", "Low"};
        spinnerPriority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities));

        final String[] selectedDate = {""};
        tvDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate[0] = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                tvDueDate.setText(selectedDate[0]);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priority = spinnerPriority.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String dueDate = selectedDate[0].isEmpty() ? "No due date" : selectedDate[0];
                    String email = FirebaseAuth.getInstance().getCurrentUser() != null ?
                            FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

                    java.util.Map<String, Object> taskData = new java.util.HashMap<>();
                    taskData.put("title", title);
                    taskData.put("description", description.isEmpty() ? "No description" : description);
                    taskData.put("priority", priority);
                    taskData.put("status", "Pending");
                    taskData.put("dueDate", dueDate);
                    taskData.put("progress", 0);
                    taskData.put("subtasksCompleted", 0);
                    taskData.put("subtasksTotal", 0);
                    taskData.put("createdAt", System.currentTimeMillis());
                    taskData.put("workerEmail", email != null ? email : "");

                    db.collection("users").document(uid)
                            .collection("tasks").add(taskData)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
                                loadTasksFromFirestore();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadTasksFromFirestore() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("tasks")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allTasks.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String priority = doc.getString("priority");
                        String status = doc.getString("status");
                        String dueDate = doc.getString("dueDate");
                        Long progressLong = doc.getLong("progress");
                        Long subtasksCompletedLong = doc.getLong("subtasksCompleted");
                        Long subtasksTotalLong = doc.getLong("subtasksTotal");

                        int progress = progressLong != null ? progressLong.intValue() : 0;
                        int subtasksCompleted = subtasksCompletedLong != null ? subtasksCompletedLong.intValue() : 0;
                        int subtasksTotal = subtasksTotalLong != null ? subtasksTotalLong.intValue() : 0;

                        if (title == null) title = "Untitled Task";
                        if (description == null) description = "No description";
                        if (priority == null) priority = "Medium";
                        if (status == null) status = "Pending";
                        if (dueDate == null) dueDate = "No due date";

                        String subtaskText = subtasksTotal > 0 ?
                                subtasksCompleted + "/" + subtasksTotal + " subtasks" : "No subtasks";

                        Task task = new Task(id, title, description, priority, status, dueDate, subtaskText, progress);
                        allTasks.add(task);
                    }
                    applyFiltersAndSearch();
                    updateStats();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show());
    }

    private void applyFiltersAndSearch() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        filteredTasks.clear();

        for (Task task : allTasks) {
            boolean matchesPriority = currentFilter.equals("All") ||
                    task.getPriority().equalsIgnoreCase(currentFilter);
            boolean matchesSearch = query.isEmpty() ||
                    task.getTitle().toLowerCase().contains(query) ||
                    task.getDescription().toLowerCase().contains(query);

            if (matchesPriority && matchesSearch) {
                filteredTasks.add(task);
            }
        }

        taskAdapter.notifyDataSetChanged();

        if (filteredTasks.isEmpty() && !allTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerTasks.setVisibility(View.GONE);
        } else if (allTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerTasks.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerTasks.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        int total = allTasks.size();
        int done = 0, active = 0, overdue = 0;
        int totalProgress = 0;

        for (Task task : allTasks) {
            totalProgress += task.getProgress();
            switch (task.getStatus()) {
                case "Completed":
                    done++;
                    break;
                case "Overdue":
                    overdue++;
                    break;
                default:
                    active++;
                    break;
            }
        }

        int overallPct = total > 0 ? totalProgress / total : 0;

        tvStatTotal.setText(String.valueOf(total));
        tvStatActive.setText(String.valueOf(active));
        tvStatDone.setText(String.valueOf(done));
        tvStatOverdue.setText(String.valueOf(overdue));
        tvOverallPercent.setText(getString(R.string.overall_percent_format, overallPct));
        overallProgress.setProgress(overallPct);
    }
}
