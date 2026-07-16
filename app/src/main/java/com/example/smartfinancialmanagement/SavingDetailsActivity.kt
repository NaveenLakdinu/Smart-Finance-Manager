package com.example.smartfinancialmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.smartfinancialmanagement.ui.GoalProgressScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SavingDetailsActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var savingId: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savingId = intent.getStringExtra("SAVING_ID") ?: return finish()

        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid ?: "test_user"

        setContent {
            var savingModel by remember { mutableStateOf<SavingModel?>(null) }

            DisposableEffect(savingId) {
                val listener = db.collection("users").document(userId).collection("savings")
                    .document(savingId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Toast.makeText(this@SavingDetailsActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val model = snapshot.toObject(SavingModel::class.java)
                            savingModel = model
                        }
                    }
                onDispose {
                    listener.remove()
                }
            }

            MaterialTheme {
                if (savingModel != null) {
                    val currentSaved = savingModel!!.currentAmount
                    val targetAmount = savingModel!!.targetAmount
                    val monthlyReq = savingModel!!.monthlySavingAmount
                    
                    GoalProgressScreen(
                        goalName = savingModel!!.savingTitle ?: "Goal",
                        targetAmount = targetAmount,
                        currentSavedParam = currentSaved,
                        monthlyRequirement = monthlyReq,
                        startDate = savingModel!!.startDate ?: "--",
                        targetDate = savingModel!!.targetDate ?: "--",
                        onDeleteGoal = { showDeleteDialog() },
                        onEditGoal = {
                            val editIntent = Intent(this@SavingDetailsActivity, SavingUpdateGoalActivity::class.java)
                            editIntent.putExtra("SAVING_ID", savingId)
                            startActivity(editIntent)
                        },
                        onBack = { finish() },
                        onAddEntry = { amount, _ -> 
                            val newTotal = currentSaved + amount
                            db.collection("users").document(userId).collection("savings")
                                .document(savingId)
                                .update("currentAmount", newTotal)
                        }
                    )
                }
            }
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this saving goal?")
            .setPositiveButton("Delete") { _, _ -> deleteSavingGoal() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSavingGoal() {
        db.collection("users").document(userId).collection("savings")
            .document(savingId)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Saving goal deleted", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
