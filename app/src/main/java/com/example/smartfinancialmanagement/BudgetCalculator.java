package com.example.smartfinancialmanagement;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetCalculator {

    public BudgetModel calculateBudget(double allowance, double scholarship, double partTime, int duration) {
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

        // Financial Score & Health Status
        int score = 70;
        if (scholarship > 0) score += 10;
        if (partTime > 0) score += 10;
        if (monthlyIncome < 15000) score -= 20;
        if (duration >= 6) score -= 5;
        
        score = Math.max(0, Math.min(100, score)); // clamp 0-100
        model.setFinancialScore(score);

        String status;
        if (score >= 90) status = "Excellent Financial Health";
        else if (score >= 75) status = "Very Good Financial Health";
        else if (score >= 60) status = "Good Financial Health";
        else if (score >= 40) status = "Needs Better Budgeting";
        else status = "Poor Financial Health";
        model.setHealthStatus(status);

        // Quick Insights
        long dailyRounded = Math.round(dailyBudget);
        model.setInsight1("You can safely spend Rs." + dailyRounded + "/day.");
        
        if (duration >= 6) {
            model.setInsight2("Current budget can last the entire semester.");
        } else {
            model.setInsight2("Current budget covers your selected semester.");
        }
        
        long saveAmount = Math.round(monthlyBudget * 0.15);
        model.setInsight3("Consider saving Rs." + saveAmount + " (15%) monthly.");

        // Semester Date Range
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.US);
        model.setSemesterStart(sdf.format(cal.getTime()));
        
        cal.add(Calendar.MONTH, duration);
        model.setSemesterEnd(sdf.format(cal.getTime()));

        return model;
    }
}
