package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing a monthly category budget limit.
 */
public class Budget {
    private int budgetId;
    private int userId;
    private String category;
    private String budgetMonth; // e.g. "2026-11"
    private BigDecimal budgetLimit;
    private LocalDateTime createdAt;

    // Computed transient field for convenience in UI/Services
    private double spentAmount;

    public Budget() {}

    public Budget(int budgetId, int userId, String category, String budgetMonth,
                  BigDecimal budgetLimit, LocalDateTime createdAt) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.category = category;
        this.budgetMonth = budgetMonth;
        this.budgetLimit = budgetLimit;
        this.createdAt = createdAt;
    }

    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(String budgetMonth) { this.budgetMonth = budgetMonth; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    public double getRemainingAmount() {
        if (budgetLimit == null) return 0.0;
        return Math.max(0.0, budgetLimit.doubleValue() - spentAmount);
    }

    public double getPercentageConsumed() {
        if (budgetLimit == null || budgetLimit.doubleValue() <= 0) return 0.0;
        return (spentAmount / budgetLimit.doubleValue()) * 100.0;
    }

    public boolean isExceeded() {
        if (budgetLimit == null) return false;
        return spentAmount > budgetLimit.doubleValue();
    }

    public boolean isOverBudget() {
        return isExceeded();
    }
}
