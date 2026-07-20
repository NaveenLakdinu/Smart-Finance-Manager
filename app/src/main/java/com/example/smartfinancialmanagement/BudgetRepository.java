package com.example.smartfinancialmanagement;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BudgetRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public BudgetRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public Task<DocumentReference> saveBudgetPlan(BudgetModel budgetModel) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            budgetModel.setUserId(user.getUid());
            return db.collection("users").document(user.getUid()).collection("budgetPlans").add(budgetModel);
        }
        return db.collection("budgetPlans").add(budgetModel);
    }

    public Task<com.google.firebase.firestore.QuerySnapshot> getUserSavings() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return db.collection("users").document(user.getUid()).collection("savings").get();
        }
        return com.google.android.gms.tasks.Tasks.forException(new Exception("User not logged in"));
    }
}
