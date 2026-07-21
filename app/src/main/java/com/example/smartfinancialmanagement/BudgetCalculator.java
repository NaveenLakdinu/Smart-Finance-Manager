package com.example.smartfinancialmanagement;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetCalculator {

    public BudgetModel calculateBudget(double allowance, double scholarship, double partTime, int duration,
                                       double totalSavings, double actualSpending, double targetGoal, double currentSaving, double entertainmentSpending) {
        BudgetModel model = new BudgetModel();
        model.setMonthlyAllowance(allowance);
        model.setScholarship(scholarship);
        model.setPartTimeIncome(partTime);
        model.setDuration(duration);

        // Calculate Monthly Income
        double monthlyIncome = allowance + scholarship + partTime;
        model.setMonthlyIncome(monthlyIncome);

        // Calculate Semester Income
        double semesterIncome = monthlyIncome * duration;
        model.setSemesterIncome(semesterIncome);

        // Daily Budget
        int totalDays = duration * 30;
        double dailyBudget = semesterIncome / totalDays;
        model.setDailyBudget(dailyBudget);

        // Weekly Budget
        double weeklyBudget = dailyBudget * 7;
        model.setWeeklyBudget(weeklyBudget);

        // Monthly Budget
        double monthlyBudget = semesterIncome / duration;
        model.setMonthlyBudget(monthlyBudget);

        // --- Calculate Financial Score (Out of 100) ---
        
        // A. Savings Rate (40 Points)
        int savingsRateScore = 10; // Default below 10%
        if (semesterIncome > 0) {
            double savingsRate = (totalSavings / semesterIncome) * 100;
            if (savingsRate >= 20) savingsRateScore = 40;
            else if (savingsRate >= 15) savingsRateScore = 30;
            else if (savingsRate >= 10) savingsRateScore = 20;
        }

        // B. Budget Control (30 Points)
        int budgetControlScore = 30; // Default stays within budget
        if (actualSpending > monthlyBudget) {
            // Determine if exceeded or far exceeded (let's say > 20% is far exceeded)
            if (actualSpending > monthlyBudget * 1.2) {
                budgetControlScore = 10; // far exceeded
            } else {
                budgetControlScore = 20; // exceeded
            }
        }

        // C. Goal Progress (20 Points)
        int goalProgressScore = 5; // Default below 40%
        if (targetGoal > 0) {
            double progress = (currentSaving / targetGoal) * 100;
            if (progress >= 80) goalProgressScore = 20;
            else if (progress >= 60) goalProgressScore = 15;
            else if (progress >= 40) goalProgressScore = 10;
        }

        // D. Expense Management (10 Points)
        int expenseScore = 5; // Default too much
        if (semesterIncome > 0) {
            double entertainmentPercent = (entertainmentSpending / semesterIncome) * 100;
            if (entertainmentPercent <= 5) expenseScore = 10;
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
        double expectedSemesterSpending = actualSpending * duration;
        if (expectedSemesterSpending <= semesterIncome) {
            model.setInsight2("Current budget can last the entire semester.");
        } else {
            model.setInsight2("Warning: Budget may run out before semester ends.");
        }
        
        // Insight 3
        long saveAmount = Math.round(monthlyBudget * 0.15);
        model.setInsight3("Consider saving Rs." + saveAmount + " monthly.");

        // Semester Date Range
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.US);
        model.setSemesterStart(sdf.format(cal.getTime()));
        
        cal.add(Calendar.MONTH, duration);
        model.setSemesterEnd(sdf.format(cal.getTime()));

        return model;
    }
}
