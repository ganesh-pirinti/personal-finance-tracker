package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates computed financial totals, category distributions, and trends.
 */
public class FinancialSummary {
    private double totalIncome;
    private double totalExpense;
    private double availableBalance;
    private String periodLabel;

    private Map<String, Double> categoryExpenses = new LinkedHashMap<>();
    private Map<String, Double> monthlyIncomeTrend = new LinkedHashMap<>();
    private Map<String, Double> monthlyExpenseTrend = new LinkedHashMap<>();

    private String highestCategory = "N/A";
    private double highestCategoryAmount = 0.0;
    private double averageExpense = 0.0;
    private int transactionCount = 0;

    public FinancialSummary() {}

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public double getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(double availableBalance) { this.availableBalance = availableBalance; }

    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }

    public Map<String, Double> getCategoryExpenses() { return categoryExpenses; }
    public void setCategoryExpenses(Map<String, Double> categoryExpenses) {
        this.categoryExpenses = categoryExpenses != null ? categoryExpenses : new LinkedHashMap<>();
    }

    public Map<String, Double> getMonthlyIncomeTrend() { return monthlyIncomeTrend; }
    public void setMonthlyIncomeTrend(Map<String, Double> monthlyIncomeTrend) {
        this.monthlyIncomeTrend = monthlyIncomeTrend != null ? monthlyIncomeTrend : new LinkedHashMap<>();
    }

    public Map<String, Double> getMonthlyExpenseTrend() { return monthlyExpenseTrend; }
    public void setMonthlyExpenseTrend(Map<String, Double> monthlyExpenseTrend) {
        this.monthlyExpenseTrend = monthlyExpenseTrend != null ? monthlyExpenseTrend : new LinkedHashMap<>();
    }

    public String getHighestCategory() { return highestCategory; }
    public void setHighestCategory(String highestCategory) { this.highestCategory = highestCategory; }

    public double getHighestCategoryAmount() { return highestCategoryAmount; }
    public void setHighestCategoryAmount(double highestCategoryAmount) { this.highestCategoryAmount = highestCategoryAmount; }

    public double getAverageExpense() { return averageExpense; }
    public void setAverageExpense(double averageExpense) { this.averageExpense = averageExpense; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public double getSavingsRate() {
        if (totalIncome <= 0) return 0.0;
        double savings = totalIncome - totalExpense;
        return (savings / totalIncome) * 100.0;
    }
}
