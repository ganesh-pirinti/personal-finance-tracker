package service;

import dao.TransactionDAO;
import model.FinancialSummary;
import model.Transaction;
import util.DateUtils;
import util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business logic service for financial transactions and analytics.
 */
public class TransactionService {
    private final TransactionDAO transactionDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
    }

    public TransactionService(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public Transaction addTransaction(int userId, String type, BigDecimal amount,
                                     String category, String description, LocalDate date)
            throws SQLException, IllegalArgumentException {
        validateTransaction(type, amount, category, description, date);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setTransactionType(type.toUpperCase());
        tx.setAmount(amount);
        tx.setCategory(category.trim());
        tx.setDescription(description.trim());
        tx.setTransactionDate(date);

        return transactionDAO.create(tx);
    }

    public boolean updateTransaction(int transactionId, int userId, String type,
                                    BigDecimal amount, String category, String description,
                                    LocalDate date) throws SQLException, IllegalArgumentException {
        validateTransaction(type, amount, category, description, date);

        Transaction tx = new Transaction();
        tx.setTransactionId(transactionId);
        tx.setUserId(userId);
        tx.setTransactionType(type.toUpperCase());
        tx.setAmount(amount);
        tx.setCategory(category.trim());
        tx.setDescription(description.trim());
        tx.setTransactionDate(date);

        return transactionDAO.update(tx);
    }

    public boolean deleteTransaction(int transactionId, int userId) throws SQLException {
        return transactionDAO.delete(transactionId, userId);
    }

    public List<Transaction> getRecentTransactions(int userId, int limit) throws SQLException {
        return transactionDAO.getRecent(userId, limit);
    }

    public List<Transaction> getFilteredTransactions(int userId, String typeFilter,
                                                    String categoryFilter, String keyword,
                                                    LocalDate startDate, LocalDate endDate,
                                                    String sortBy) throws SQLException {
        return transactionDAO.findFiltered(userId, typeFilter, categoryFilter, keyword, startDate, endDate, sortBy);
    }

    public FinancialSummary getSummary(int userId, String monthKey) throws SQLException {
        FinancialSummary summary = new FinancialSummary();
        summary.setPeriodLabel(monthKey != null && !monthKey.isEmpty() ?
                DateUtils.formatMonthKeyDisplay(monthKey) : "All Time");

        double income = transactionDAO.getTotalAmount(userId, Transaction.TYPE_INCOME, monthKey);
        double expense = transactionDAO.getTotalAmount(userId, Transaction.TYPE_EXPENSE, monthKey);
        summary.setTotalIncome(income);
        summary.setTotalExpense(expense);
        summary.setAvailableBalance(income - expense);

        // Category breakdown
        Map<String, Double> catMap = transactionDAO.getCategoryExpenses(userId, monthKey);
        summary.setCategoryExpenses(catMap);

        // Highest spending category
        String highCat = "None";
        double highAmount = 0.0;
        double sumExpense = 0.0;
        int count = 0;
        for (Map.Entry<String, Double> entry : catMap.entrySet()) {
            if (entry.getValue() > highAmount) {
                highAmount = entry.getValue();
                highCat = entry.getKey();
            }
            sumExpense += entry.getValue();
            count++;
        }
        summary.setHighestCategory(highCat);
        summary.setHighestCategoryAmount(highAmount);
        summary.setAverageExpense(count > 0 ? (sumExpense / count) : 0.0);

        // 6-Month Trend Data for charts
        List<String> last6 = DateUtils.getLastMonths(6);
        Map<String, double[]> monthlyTrends = transactionDAO.getMonthlyTrends(userId, last6);
        for (String m : last6) {
            double[] pair = monthlyTrends.getOrDefault(m, new double[]{0.0, 0.0});
            summary.getMonthlyIncomeTrend().put(m, pair[0]);
            summary.getMonthlyExpenseTrend().put(m, pair[1]);
        }

        return summary;
    }

    private void validateTransaction(String type, BigDecimal amount, String category,
                                    String description, LocalDate date) {
        if (!Transaction.TYPE_INCOME.equalsIgnoreCase(type) &&
            !Transaction.TYPE_EXPENSE.equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Transaction type must be INCOME or EXPENSE.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (!InputValidator.isNotEmpty(category)) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (!InputValidator.isNotEmpty(description)) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Transaction date is required.");
        }
    }
}
