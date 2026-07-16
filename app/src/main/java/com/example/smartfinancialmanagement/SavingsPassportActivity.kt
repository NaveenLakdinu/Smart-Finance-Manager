package com.example.smartfinancialmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.smartfinancialmanagement.ui.AchievementManager
import com.example.smartfinancialmanagement.ui.SavingsColors
import com.example.smartfinancialmanagement.ui.SavingsGoalUI
import com.example.smartfinancialmanagement.ui.SavingsPassportScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class SavingsPassportActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = auth.currentUser?.uid ?: "test_user"

        setContent {
            var savingsList by remember { mutableStateOf<List<SavingModel>>(emptyList()) }

            DisposableEffect(userId) {
                val listener = db.collection("users").document(userId).collection("savings")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Toast.makeText(this@SavingsPassportActivity, "Failed to load goals", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            savingsList = snapshot.documents.mapNotNull { it.toObject(SavingModel::class.java) }
                        }
                    }
                onDispose { listener.remove() }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val achievements = AchievementManager.evaluateAchievements(savingsList)
                    val totalSavings = savingsList.sumOf { it.currentAmount }
                    val level = AchievementManager.getSavingsLevel(totalSavings)

                    val goalsUi = savingsList.map { model ->
                        val percent = if (model.targetAmount > 0) ((model.currentAmount / model.targetAmount) * 100).toInt().coerceIn(0, 100) else 0
                        val remaining = maxOf(0.0, model.targetAmount - model.currentAmount)

                        SavingsGoalUI(
                            title = model.savingTitle ?: "Goal",
                            emoji = "🎯",
                            iconBg = Color(0xFFE7F0FA),
                            iconTint = Color(0xFF2C6FA6),
                            dueText = "Target: ${model.targetDate ?: "--"}",
                            percent = percent,
                            savedLabel = "${money(model.currentAmount)} saved",
                            totalLabel = "of ${money(model.targetAmount)}",
                            remainingLabel = money(remaining),
                            perMonthLabel = money(model.monthlySavingAmount),
                            progressColor = SavingsColors.Emerald
                        )
                    }

                    SavingsPassportScreen(
                        savingsLevel = level,
                        earnedAchievements = achievements.first,
                        lockedAchievements = achievements.second,
                        goals = goalsUi,
                        onBackClick = { finish() },
                        onAddGoalClick = {
                            startActivity(Intent(this@SavingsPassportActivity, SavingAddGoalActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun money(amount: Double): String {
        return "LKR " + NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
    }
}
