package com.example.smartfinancialmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.smartfinancialmanagement.ui.Goal
import com.example.smartfinancialmanagement.ui.GoalStatus
import com.example.smartfinancialmanagement.ui.SavingManagerScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavingManagerActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId: String = "test_user"

    private val goalsList = mutableStateListOf<Goal>()
    private var totalSavingsGoal by mutableDoubleStateOf(0.0)

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userId = auth.currentUser?.uid ?: "test_user"

        fetchSavingsData()

        setContent {
            SavingManagerScreen(
                totalSavingsGoal = totalSavingsGoal,
                goals = goalsList,
                onBack = { finish() },
                onAddGoal = { startActivity(Intent(this, SavingAddGoalActivity::class.java)) },
                onAllGoals = { startActivity(Intent(this, SavingListActivity::class.java)) },
                onAchievements = { startActivity(Intent(this, SavingsPassportActivity::class.java)) },
                onReport = { startActivity(Intent(this, SavingGenerateReportActivity::class.java)) },
                onSeeAllActive = { startActivity(Intent(this, SavingListActivity::class.java)) },
                onGoalClick = { goal ->
                    val intent = Intent(this, SavingDetailsActivity::class.java)
                    intent.putExtra("SAVING_ID", goal.id)
                    startActivity(intent)
                }
            )
        }
    }

    private fun fetchSavingsData() {
        db.collection("users").document(userId).collection("savings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                goalsList.clear()
                var totalTarget = 0.0

                snapshot?.documents?.forEach { document ->
                    val saving = document.toObject(SavingModel::class.java)
                    if (saving != null) {
                        totalTarget += saving.targetAmount

                        val isCompleted = saving.currentAmount >= saving.targetAmount
                        var isPassedTargetDate = false
                        try {
                            val targetDate = dateFormat.parse(saving.targetDate)
                            if (targetDate != null && targetDate.before(Date())) {
                                isPassedTargetDate = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val status = when {
                            isCompleted -> GoalStatus.COMPLETED
                            isPassedTargetDate -> GoalStatus.INCOMPLETE
                            else -> GoalStatus.ONGOING
                        }

                        goalsList.add(
                            Goal(
                                id = saving.savingId,
                                name = saving.savingTitle,
                                icon = "🎯", // Default icon
                                target = saving.targetAmount,
                                current = saving.currentAmount,
                                status = status
                            )
                        )
                    }
                }
                totalSavingsGoal = totalTarget
            }
    }
}
