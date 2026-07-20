package com.example.smartfinancialmanagement;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetCalculator {

    public BudgetModel calculateBudget(double totalWalletBalance, double ignored1, double ignored2, int durationDays,
                                       double totalSavings, double actualSpending, double targetGoal, double currentSaving, double entertainmentSpending) {
        BudgetModel model = new BudgetModel();
        
        // We'll store the original provided parameters in the model for reference
        model.setMonthlyAllowance(totalWalletBalance);
        model.setScholarship(ignored1);
        model.setPartTimeIncome(ignored2);
        
        // Store the duration in days in the duration field
        model.setDuration(durationDays);

        // Calculate Period Income (Total budget available for the duration)
        double periodIncome = totalWalletBalance;
        
        // We'll repurpose 'semesterIncome' to hold the total income for the period
        model.setSemesterIncome(periodIncome);
        // And also store it as 'monthlyIncome' to avoid breaking UI that expects it
        model.setMonthlyIncome(periodIncome);

        // Daily Budget
        int totalDays = durationDays > 0 ? durationDays : 1;
        double dailyBudget = periodIncome / totalDays;
        model.setDailyBudget(dailyBudget);

        // Weekly Budget
        double weeklyBudget = dailyBudget * 7;
        model.setWeeklyBudget(weeklyBudget);

        // Monthly Budget
        double monthlyBudget = dailyBudget * 30;
        model.setMonthlyBudget(monthlyBudget);

        // --- Calculate Financial Score (Out of 100) ---
        
        // A. Savings Rate (40 Points)
        int savingsRateScore = 0;
        if (periodIncome > 0) {
            double savingsRate = (totalSavings / periodIncome) * 100;
            savingsRateScore = (int) Math.min(40, (savingsRate / 20.0) * 40);
        }

        // B. Budget Control (30 Points)
        int budgetControlScore = 30;
        if (actualSpending > 0 && periodIncome > 0) {
            double spendingRatio = actualSpending / periodIncome;
            if (spendingRatio <= 1.0) {
                budgetControlScore = 30 - (int) (spendingRatio * 10);
            } else if (spendingRatio <= 1.2) {
                budgetControlScore = Math.max(0, 20 - (int) (((spendingRatio - 1.0) / 0.2) * 20));
            } else {
                budgetControlScore = 0;
            }
        }

        // C. Goal Progress (20 Points)
        int goalProgressScore = 0;
        if (targetGoal > 0) {
            double progress = (currentSaving / targetGoal) * 100;
            goalProgressScore = (int) Math.min(20, (progress / 100.0) * 20);
        }

        // D. Expense Management (10 Points)
        int expenseScore = 10;
        if (periodIncome > 0 && entertainmentSpending > 0) {
            double entertainmentPercent = (entertainmentSpending / periodIncome) * 100;
            if (entertainmentPercent > 5) {
                expenseScore = Math.max(0, 10 - (int) (entertainmentPercent - 5));
            }
        }

        int totalScore = savingsRateScore + budgetControlScore + goalProgressScore + expenseScore;
        totalScore = Math.max(0, Math.min(100, totalScore)); // clamp 0-100
        model.setFinancialScore(totalScore);

        // --- Health Categories ---
        String status;
        if (totalScore >= 90) status = "Excellent";
        else if (totalScore >= 75) status = "Very Good";
        else if (totalScore >= 60) status = "Good";
        else if (totalScore >= 40) status = "Average";
        else status = "Needs Improvement";
        model.setHealthStatus(status);

        // --- Quick Insights ---
        
        // Insight 1
        long dailyRounded = Math.round(dailyBudget);
        model.setInsight1("You can safely spend Rs." + dailyRounded + "/day.");
        
        // Insight 2
        double expectedPeriodSpending = (actualSpending / 30) * durationDays; // Rough estimate based on past 30 days
        if (expectedPeriodSpending <= periodIncome) {
            model.setInsight2("Current budget can last the entire period.");
        } else {
            model.setInsight2("Warning: Budget may run out before period ends.");
        }
        
        // Insight 3
        long saveAmount = Math.round(monthlyBudget * 0.15);
        model.setInsight3("Consider saving Rs." + saveAmount + " monthly.");

        // Period Date Range
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        model.setSemesterStart(sdf.format(cal.getTime()));
        
        cal.add(Calendar.DAY_OF_YEAR, durationDays);
        model.setSemesterEnd(sdf.format(cal.getTime()));

        return model;
    }
}
