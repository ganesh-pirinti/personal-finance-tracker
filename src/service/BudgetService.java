package service;

import dao.BudgetDAO;
import dao.TransactionDAO;
import model.Budget;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing category budgets and tracking real-time spending progress.
 */
public class BudgetService {
    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public BudgetService(BudgetDAO budgetDAO, TransactionDAO transactionDAO) {
        this.budgetDAO = budgetDAO;
        this.transactionDAO = transactionDAO;
    }

    public Budget setBudget(int userId, String category, String monthKey, BigDecimal limit)
            throws SQLException, IllegalArgumentException {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        if (monthKey == null || !monthKey.matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("Month must be in YYYY-MM format.");
        }
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than zero.");
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategory(category.trim());
        budget.setBudgetMonth(monthKey.trim());
        budget.setBudgetLimit(limit);

        return budgetDAO.saveOrUpdate(budget);
    }

    public boolean deleteBudget(int budgetId, int userId) throws SQLException {
        return budgetDAO.delete(budgetId, userId);
    }

    public List<Budget> getBudgetsForMonth(int userId, String monthKey) throws SQLException {
        List<Budget> budgets = budgetDAO.findByUserAndMonth(userId, monthKey);
        for (Budget b : budgets) {
            double spent = transactionDAO.getCategoryExpenseForMonth(userId, b.getCategory(), monthKey);
            b.setSpentAmount(spent);
        }
        return budgets;
    }
}
