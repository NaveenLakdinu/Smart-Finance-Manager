package com.example.smartfinancialmanagement;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class WorkerTasksActivity extends AppCompatActivity {

    // Search
    private EditText etSearch;
    private ImageView btnClearSearch;

    // Filters
    private TextView filterAll, filterHigh, filterMedium, filterLow;
    private String currentFilter = "All";

    // Stats
    private TextView tvStatTotal, tvStatActive, tvStatDone, tvStatOverdue, tvOverallPercent;
    private ProgressBar overallProgress;

    // Fixed 'Field may be final' warnings
    private final MaterialCardView[] cards = new MaterialCardView[4];
    private final CheckBox[] checkboxes = new CheckBox[4];
    private final TextView[] titles = new TextView[4];

    // Data Model for Logic
    private final String[] baseStatuses = {"In Progress", "Pending", "Completed", "Overdue"};
    private final int[] baseProgress = {65, 0, 100, 30};
    private final String[] priorities = {"High", "Medium", "Low", "High"};

    // State tracking
    private final boolean[] isCompleted = {false, false, true, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_tasks);

        initViews();
        setupBackPressed();
        setupSearchAndClear();
        setupFilters();
        setupCheckboxes();

        for (int i = 0; i < 4; i++) {
            updateTaskAppearance(i);
        }
        updateStats();
        applyFiltersAndSearch();
    }

    private void initViews() {
        // Fixed 'Field can be converted to local variable' and 'cut & paste error'
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnAdd = findViewById(R.id.btnAdd);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnAdd.setOnClickListener(v -> {
            // Add task logic here
        });

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

        cards[0] = findViewById(R.id.cardTask1);
        cards[1] = findViewById(R.id.cardTask2);
        cards[2] = findViewById(R.id.cardTask3);
        cards[3] = findViewById(R.id.cardTask4);

        checkboxes[0] = findViewById(R.id.cbTask1);
        checkboxes[1] = findViewById(R.id.cbTask2);
        checkboxes[2] = findViewById(R.id.cbTask3);
        checkboxes[3] = findViewById(R.id.cbTask4);

        titles[0] = findViewById(R.id.tvTitle1);
        titles[1] = findViewById(R.id.tvTitle2);
        titles[2] = findViewById(R.id.tvTitle3);
        titles[3] = findViewById(R.id.tvTitle4);
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
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

    private void setupCheckboxes() {
        for (int i = 0; i < 4; i++) {
            final int index = i;
            checkboxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                isCompleted[index] = isChecked;
                updateTaskAppearance(index);
                updateStats();
            });
        }
    }

    private void updateTaskAppearance(int index) {
        if (isCompleted[index]) {
            cards[index].setAlpha(0.5f);
            titles[index].setPaintFlags(titles[index].getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            cards[index].setAlpha(1.0f);
            titles[index].setPaintFlags(titles[index].getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void updateStats() {
        int done = 0, active = 0, overdue = 0, totalProgress = 0;

        for (int i = 0; i < 4; i++) {
            String currentStatus = isCompleted[i] ? "Completed" : baseStatuses[i];
            int currentProgress = isCompleted[i] ? 100 : baseProgress[i];

            totalProgress += currentProgress;

            switch (currentStatus) {
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

        int overallPct = totalProgress / 4;

        tvStatTotal.setText("4");
        tvStatActive.setText(String.valueOf(active));
        tvStatDone.setText(String.valueOf(done));
        tvStatOverdue.setText(String.valueOf(overdue));

        // Fixed 'Do not concatenate text' warning
        tvOverallPercent.setText(getString(R.string.overall_percent_format, overallPct));
        overallProgress.setProgress(overallPct);
    }

    private void applyFiltersAndSearch() {
        String query = etSearch.getText().toString().toLowerCase().trim();

        for (int i = 0; i < 4; i++) {
            boolean matchesPriority = currentFilter.equals("All") || priorities[i].equals(currentFilter);
            boolean matchesSearch = query.isEmpty() ||
                    titles[i].getText().toString().toLowerCase().contains(query);

            if (matchesPriority && matchesSearch) {
                cards[i].setVisibility(View.VISIBLE);
            } else {
                cards[i].setVisibility(View.GONE);
            }
        }
    }
}
