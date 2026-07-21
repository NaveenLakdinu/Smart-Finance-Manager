package com.example.smartfinancialmanagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

public class BudgetViewModel extends ViewModel {
    private final BudgetRepository repository;
    private final BudgetCalculator calculator;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<BudgetModel> calculationSuccess = new MutableLiveData<>();

    public BudgetViewModel() {
        repository = new BudgetRepository();
        calculator = new BudgetCalculator();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<BudgetModel> getCalculationSuccess() { return calculationSuccess; }

    public void calculateAndSaveBudget(double allowance, double scholarship, double partTime, int duration) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            errorMessage.setValue("Error: User not logged in.");
            return;
        }

        isLoading.setValue(true);

        repository.getUserSavings().addOnCompleteListener(fetchTask -> {
            if (!fetchTask.isSuccessful()) {
                isLoading.setValue(false);
                errorMessage.setValue(fetchTask.getException() != null ? fetchTask.getException().getMessage() : "Failed to fetch savings");
                return;
            }

            double totalSavings = 0;
            double targetGoal = 0;
            double currentSaving = 0;

            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : fetchTask.getResult()) {
                SavingModel saving = doc.toObject(SavingModel.class);
                totalSavings += saving.getCurrentAmount();
                targetGoal += saving.getTargetAmount();
                currentSaving += saving.getCurrentAmount();
            }

            // Defaults for currently untracked fields
            double actualSpending = 0;
            double entertainmentSpending = 0;

            try {
                BudgetModel model = calculator.calculateBudget(allowance, scholarship, partTime, duration,
                        totalSavings, actualSpending, targetGoal, currentSaving, entertainmentSpending);
                
                repository.saveBudgetPlan(model).addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        DocumentReference docRef = task.getResult();
                        if (docRef != null) {
                            model.setDocumentId(docRef.getId());
                        }
                        calculationSuccess.setValue(model);
                    } else {
                        errorMessage.setValue(task.getException() != null ? task.getException().getMessage() : "Failed to save budget plan");
                    }
                });
            } catch (Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue("Error calculating budget: " + e.getMessage());
            }
        });
    }
}
