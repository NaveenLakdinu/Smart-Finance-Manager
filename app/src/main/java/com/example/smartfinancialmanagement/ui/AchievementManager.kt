package com.example.smartfinancialmanagement.ui

import com.example.smartfinancialmanagement.SavingModel

object AchievementManager {

    fun evaluateAchievements(savings: List<SavingModel>): Pair<List<Achievement>, List<Achievement>> {
        val totalSavings = savings.sumOf { it.currentAmount }
        val completedGoalsCount = savings.count { it.currentAmount >= it.targetAmount && it.targetAmount > 0 }
        
        val allBadges = mutableListOf<Achievement>()

        // 1. Bronze Saver
        val bronzeProgress = (totalSavings / 5000.0 * 100).toInt().coerceIn(0, 100)
        allBadges.add(
            Achievement(
                id = "bronze_saver",
                title = "Bronze Saver",
                subtitle = "Save Rs. 5,000",
                emoji = "🥉",
                medalColor = SavingsColors.Bronze,
                earned = totalSavings >= 5000.0,
                progressPercent = bronzeProgress
            )
        )

        // 2. Silver Saver
        val silverProgress = (totalSavings / 25000.0 * 100).toInt().coerceIn(0, 100)
        allBadges.add(
            Achievement(
                id = "silver_saver",
                title = "Silver Saver",
                subtitle = "Save Rs. 25,000",
                emoji = "🥈",
                medalColor = SavingsColors.Silver,
                earned = totalSavings >= 25000.0,
                progressPercent = silverProgress
            )
        )

        // 3. Gold Saver
        val goldProgress = (totalSavings / 50000.0 * 100).toInt().coerceIn(0, 100)
        allBadges.add(
            Achievement(
                id = "gold_saver",
                title = "Gold Saver",
                subtitle = "Save Rs. 50,000",
                emoji = "🥇",
                medalColor = SavingsColors.Brass,
                earned = totalSavings >= 50000.0,
                progressPercent = goldProgress
            )
        )

        // 4. Goal Master
        val masterProgress = (completedGoalsCount / 3.0 * 100).toInt().coerceIn(0, 100)
        allBadges.add(
            Achievement(
                id = "goal_master",
                title = "Goal Master",
                subtitle = "Complete 3 saving goals",
                emoji = "🏆",
                medalColor = SavingsColors.BrassDark,
                earned = completedGoalsCount >= 3,
                progressPercent = masterProgress
            )
        )

        // 5. Consistent Saver (Hardcoded for now as discussed)
        allBadges.add(
            Achievement(
                id = "consistent_saver",
                title = "Consistent Saver",
                subtitle = "Save every month for 6 months",
                emoji = "💧",
                medalColor = SavingsColors.Emerald,
                earned = false,
                progressPercent = 16 // 1 month out of 6
            )
        )

        val earned = allBadges.filter { it.earned }
        val locked = allBadges.filter { !it.earned }

        return Pair(earned, locked)
    }

    fun getSavingsLevel(totalSavings: Double): String {
        return when {
            totalSavings >= 50000 -> "Gold Saver"
            totalSavings >= 25000 -> "Silver Saver"
            totalSavings >= 5000 -> "Bronze Saver"
            else -> "Starter"
        }
    }
}
