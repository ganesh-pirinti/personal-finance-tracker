package model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates system-wide operational, user, financial, and database health metrics.
 * Designed for admin dashboard observability with strict aggregation to protect user privacy.
 */
public class SystemMetrics {
    private int totalUsers;
    private int activeUsers;
    private int suspendedUsers;
    private int adminCount;

    private int totalTransactions;
    private BigDecimal aggregateIncome = BigDecimal.ZERO;
    private BigDecimal aggregateExpense = BigDecimal.ZERO;
    private BigDecimal aggregateNetCashFlow = BigDecimal.ZERO;

    private int totalBudgets;
    private int auditLogCount;

    private boolean databaseOnline;
    private String databaseUrl;
    private long databasePingMs;

    private Map<String, BigDecimal> categoryExpenses = new HashMap<>();
    private Map<String, Integer> userGrowthByMonth = new HashMap<>();

    public SystemMetrics() {}

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }

    public int getSuspendedUsers() { return suspendedUsers; }
    public void setSuspendedUsers(int suspendedUsers) { this.suspendedUsers = suspendedUsers; }

    public int getAdminCount() { return adminCount; }
    public void setAdminCount(int adminCount) { this.adminCount = adminCount; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public BigDecimal getAggregateIncome() { return aggregateIncome; }
    public void setAggregateIncome(BigDecimal aggregateIncome) { this.aggregateIncome = aggregateIncome; }

    public BigDecimal getAggregateExpense() { return aggregateExpense; }
    public void setAggregateExpense(BigDecimal aggregateExpense) { this.aggregateExpense = aggregateExpense; }

    public BigDecimal getAggregateNetCashFlow() { return aggregateNetCashFlow; }
    public void setAggregateNetCashFlow(BigDecimal aggregateNetCashFlow) { this.aggregateNetCashFlow = aggregateNetCashFlow; }

    public int getTotalBudgets() { return totalBudgets; }
    public void setTotalBudgets(int totalBudgets) { this.totalBudgets = totalBudgets; }

    public int getAuditLogCount() { return auditLogCount; }
    public void setAuditLogCount(int auditLogCount) { this.auditLogCount = auditLogCount; }

    public boolean isDatabaseOnline() { return databaseOnline; }
    public void setDatabaseOnline(boolean databaseOnline) { this.databaseOnline = databaseOnline; }

    public String getDatabaseUrl() { return databaseUrl; }
    public void setDatabaseUrl(String databaseUrl) { this.databaseUrl = databaseUrl; }

    public long getDatabasePingMs() { return databasePingMs; }
    public void setDatabasePingMs(long databasePingMs) { this.databasePingMs = databasePingMs; }

    public Map<String, BigDecimal> getCategoryExpenses() { return categoryExpenses; }
    public void setCategoryExpenses(Map<String, BigDecimal> categoryExpenses) { this.categoryExpenses = categoryExpenses; }

    public Map<String, Integer> getUserGrowthByMonth() { return userGrowthByMonth; }
    public void setUserGrowthByMonth(Map<String, Integer> userGrowthByMonth) { this.userGrowthByMonth = userGrowthByMonth; }
}
