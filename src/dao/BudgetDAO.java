package dao;

import database.DatabaseConnection;
import model.Budget;
import service.AuthenticationService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Budgets.
 * Uses PreparedStatement against MySQL with resilient offline fallback when database server is offline
 * or when operating in explicit demonstration / test isolation mode.
 */
public class BudgetDAO {
    private static final List<Budget> memoryBudgets = new ArrayList<>();
    private static int memoryBudgetIdCounter = 10;

    static {
        resetMemoryStore();
    }

    public static synchronized void resetMemoryStore() {
        memoryBudgets.clear();
        String currentMonth = util.DateUtils.getCurrentMonthKey();
        int uid = 1;

        memoryBudgets.add(new Budget(1, uid, "Food", currentMonth, new BigDecimal("10000.00"), LocalDateTime.now()));
        memoryBudgets.add(new Budget(2, uid, "Travel", currentMonth, new BigDecimal("5000.00"), LocalDateTime.now()));
        memoryBudgets.add(new Budget(3, uid, "Shopping", currentMonth, new BigDecimal("6000.00"), LocalDateTime.now()));
        memoryBudgets.add(new Budget(4, uid, "Bills", currentMonth, new BigDecimal("4000.00"), LocalDateTime.now()));
        memoryBudgets.add(new Budget(5, uid, "Entertainment", currentMonth, new BigDecimal("3000.00"), LocalDateTime.now()));
        memoryBudgetIdCounter = 10;
    }

    public Budget saveOrUpdate(Budget budget) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            saveOrUpdateInMemory(budget);
            return budget;
        }

        String sql = "INSERT INTO budgets (user_id, category, budget_month, budget_limit) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE budget_limit = VALUES(budget_limit)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, budget.getUserId());
            ps.setString(2, budget.getCategory());
            ps.setString(3, budget.getBudgetMonth());
            ps.setBigDecimal(4, budget.getBudgetLimit());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    budget.setBudgetId(keys.getInt(1));
                }
            }
            return budget;
        }
    }

    private synchronized void saveOrUpdateInMemory(Budget budget) {
        for (int i = 0; i < memoryBudgets.size(); i++) {
            Budget b = memoryBudgets.get(i);
            if (b.getUserId() == budget.getUserId() &&
                b.getCategory().equalsIgnoreCase(budget.getCategory()) &&
                b.getBudgetMonth().equals(budget.getBudgetMonth())) {
                b.setBudgetLimit(budget.getBudgetLimit());
                return;
            }
        }
        budget.setBudgetId(memoryBudgetIdCounter++);
        budget.setCreatedAt(LocalDateTime.now());
        memoryBudgets.add(budget);
    }

    public boolean delete(int budgetId, int userId) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            return deleteFromMemory(budgetId, userId);
        }

        String sql = "DELETE FROM budgets WHERE budget_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private synchronized boolean deleteFromMemory(int budgetId, int userId) {
        return memoryBudgets.removeIf(b -> b.getBudgetId() == budgetId && b.getUserId() == userId);
    }

    public List<Budget> findByUserAndMonth(int userId, String monthKey) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            List<Budget> list = new ArrayList<>();
            synchronized (memoryBudgets) {
                for (Budget b : memoryBudgets) {
                    if (b.getUserId() == userId && b.getBudgetMonth().equals(monthKey)) {
                        list.add(b);
                    }
                }
            }
            return list;
        }

        String sql = "SELECT budget_id, user_id, category, budget_month, budget_limit, created_at " +
                     "FROM budgets WHERE user_id = ? AND budget_month = ? ORDER BY category ASC";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Budget findByUserCategoryMonth(int userId, String category, String monthKey) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryBudgets) {
                for (Budget b : memoryBudgets) {
                    if (b.getUserId() == userId && b.getCategory().equalsIgnoreCase(category) && b.getBudgetMonth().equals(monthKey)) {
                        return b;
                    }
                }
            }
            return null;
        }

        String sql = "SELECT budget_id, user_id, category, budget_month, budget_limit, created_at " +
                     "FROM budgets WHERE user_id = ? AND category = ? AND budget_month = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setBudgetId(rs.getInt("budget_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setCategory(rs.getString("category"));
        b.setBudgetMonth(rs.getString("budget_month"));
        b.setBudgetLimit(rs.getBigDecimal("budget_limit"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            b.setCreatedAt(created.toLocalDateTime());
        }
        return b;
    }
}
