package com.example.smartfinancialmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import com.example.smartfinancialmanagement.ui.Goal
import com.example.smartfinancialmanagement.ui.GoalStatus
import com.example.smartfinancialmanagement.ui.SavingGoalsListScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavingListActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId: String = "test_user"

    private val goalsList = mutableStateListOf<Goal>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userId = auth.currentUser?.uid ?: "test_user"

        fetchSavingsData()

        setContent {
            SavingGoalsListScreen(
                goals = goalsList,
                onBack = { finish() },
                onAddGoal = { startActivity(Intent(this, SavingAddGoalActivity::class.java)) },
                onEditGoal = { goal ->
                    val intent = Intent(this, SavingUpdateGoalActivity::class.java)
                    intent.putExtra("SAVING_ID", goal.id)
                    startActivity(intent)
                },
                onDeleteGoal = { goal ->
                    showDeleteDialog(goal.id)
                },
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

                snapshot?.documents?.forEach { document ->
                    val saving = document.toObject(SavingModel::class.java)
                    if (saving != null) {
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
            }
    }

    private fun showDeleteDialog(savingId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this saving goal?")
            .setPositiveButton("Delete") { _, _ -> deleteSavingGoal(savingId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSavingGoal(savingId: String) {
        db.collection("users").document(userId).collection("savings")
            .document(savingId).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Saving goal deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
