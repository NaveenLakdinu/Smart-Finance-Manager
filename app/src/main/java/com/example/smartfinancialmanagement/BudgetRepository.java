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
        }
        return db.collection("budgetPlans").add(budgetModel);
    }
}
