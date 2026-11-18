package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model representing a financial transaction (Income or Expense).
 */
public class Transaction {
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    private int transactionId;
    private int userId;
    private String transactionType; // INCOME or EXPENSE
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Transaction() {}

    public Transaction(int transactionId, int userId, String transactionType,
                       BigDecimal amount, String category, String description,
                       LocalDate transactionDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public boolean isIncome() {
        return TYPE_INCOME.equalsIgnoreCase(transactionType);
    }

    public boolean isExpense() {
        return TYPE_EXPENSE.equalsIgnoreCase(transactionType);
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + transactionId +
                ", type='" + transactionType + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + transactionDate +
                '}';
    }
}
