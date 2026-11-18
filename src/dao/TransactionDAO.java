package dao;

import database.DatabaseConnection;
import model.Transaction;
import service.AuthenticationService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Transactions with full user-isolation and parameterized queries.
 * Includes offline memory caching fallback if local database engine is temporarily offline
 * or when operating in explicit demonstration / test isolation mode.
 */
public class TransactionDAO {
    private static final List<Transaction> memoryStore = new ArrayList<>();
    private static int memoryTxIdCounter = 100;

    static {
        resetMemoryStore();
    }

    public static synchronized void resetMemoryStore() {
        memoryStore.clear();
        LocalDate today = LocalDate.now();
        int uid = 1;

        memoryStore.add(new Transaction(1, uid, "INCOME", new BigDecimal("65000.00"), "Salary", "Monthly Software Engineering Salary", today.withDayOfMonth(1), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(2, uid, "INCOME", new BigDecimal("12500.00"), "Freelance", "Mobile App UI Design Project", today.minusDays(10), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(3, uid, "INCOME", new BigDecimal("5000.00"), "Scholarship", "Research Fellowship Stipend", today.minusDays(18), LocalDateTime.now(), LocalDateTime.now()));

        memoryStore.add(new Transaction(4, uid, "EXPENSE", new BigDecimal("18000.00"), "Education", "Semester Tuition & Lab Fee", today.minusDays(14), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(5, uid, "EXPENSE", new BigDecimal("6500.00"), "Food", "Monthly Groceries & Dining", today.minusDays(4), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(6, uid, "EXPENSE", new BigDecimal("3200.00"), "Bills", "Broadband & Electricity Utility", today.minusDays(8), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(7, uid, "EXPENSE", new BigDecimal("2400.00"), "Travel", "Metro Rail Pass & Fuel", today.minusDays(6), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(8, uid, "EXPENSE", new BigDecimal("4500.00"), "Shopping", "Tech Accessories & Books", today.minusDays(2), LocalDateTime.now(), LocalDateTime.now()));
        memoryStore.add(new Transaction(9, uid, "EXPENSE", new BigDecimal("1500.00"), "Entertainment", "Streaming & Cinema", today.minusDays(1), LocalDateTime.now(), LocalDateTime.now()));
        memoryTxIdCounter = 100;
    }

    public Transaction create(Transaction tx) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryStore) {
                tx.setTransactionId(memoryTxIdCounter++);
                tx.setCreatedAt(LocalDateTime.now());
                tx.setUpdatedAt(LocalDateTime.now());
                memoryStore.add(tx);
            }
            return tx;
        }

        String sql = "INSERT INTO transactions (user_id, transaction_type, amount, category, description, transaction_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getUserId());
            ps.setString(2, tx.getTransactionType());
            ps.setBigDecimal(3, tx.getAmount());
            ps.setString(4, tx.getCategory());
            ps.setString(5, tx.getDescription());
            ps.setDate(6, Date.valueOf(tx.getTransactionDate()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        tx.setTransactionId(keys.getInt(1));
                    }
                }
            }
            tx.setCreatedAt(LocalDateTime.now());
            tx.setUpdatedAt(LocalDateTime.now());
            return tx;
        }
    }

    public boolean update(Transaction tx) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            return updateInMemory(tx);
        }

        String sql = "UPDATE transactions SET transaction_type = ?, amount = ?, category = ?, " +
                     "description = ?, transaction_date = ? WHERE transaction_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tx.getTransactionType());
            ps.setBigDecimal(2, tx.getAmount());
            ps.setString(3, tx.getCategory());
            ps.setString(4, tx.getDescription());
            ps.setDate(5, Date.valueOf(tx.getTransactionDate()));
            ps.setInt(6, tx.getTransactionId());
            ps.setInt(7, tx.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    private boolean updateInMemory(Transaction tx) {
        synchronized (memoryStore) {
            for (int i = 0; i < memoryStore.size(); i++) {
                Transaction existing = memoryStore.get(i);
                if (existing.getTransactionId() == tx.getTransactionId() && existing.getUserId() == tx.getUserId()) {
                    tx.setUpdatedAt(LocalDateTime.now());
                    memoryStore.set(i, tx);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean delete(int transactionId, int userId) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            return deleteFromMemory(transactionId, userId);
        }

        String sql = "DELETE FROM transactions WHERE transaction_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private boolean deleteFromMemory(int transactionId, int userId) {
        synchronized (memoryStore) {
            return memoryStore.removeIf(t -> t.getTransactionId() == transactionId && t.getUserId() == userId);
        }
    }

    public Transaction findById(int transactionId, int userId) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryStore) {
                for (Transaction tx : memoryStore) {
                    if (tx.getTransactionId() == transactionId && tx.getUserId() == userId) return tx;
                }
            }
            return null;
        }

        String sql = "SELECT transaction_id, user_id, transaction_type, amount, category, description, " +
                     "transaction_date, created_at, updated_at FROM transactions WHERE transaction_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Transaction> getRecent(int userId, int limit) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            List<Transaction> userList = new ArrayList<>();
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() == userId) userList.add(t);
                }
            }
            userList.sort((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()));
            return userList.subList(0, Math.min(limit, userList.size()));
        }

        String sql = "SELECT transaction_id, user_id, transaction_type, amount, category, description, " +
                     "transaction_date, created_at, updated_at FROM transactions WHERE user_id = ? " +
                     "ORDER BY transaction_date DESC, transaction_id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Transaction> findFiltered(int userId, String typeFilter, String categoryFilter,
                                         String searchKeyword, LocalDate startDate, LocalDate endDate,
                                         String sortBy) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            List<Transaction> filtered = new ArrayList<>();
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() != userId) continue;
                    if (typeFilter != null && !typeFilter.equalsIgnoreCase("ALL") && !t.getTransactionType().equalsIgnoreCase(typeFilter)) continue;
                    if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL") && !t.getCategory().equalsIgnoreCase(categoryFilter)) continue;
                    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                        String kw = searchKeyword.toLowerCase();
                        boolean matchDesc = t.getDescription() != null && t.getDescription().toLowerCase().contains(kw);
                        boolean matchCat = t.getCategory() != null && t.getCategory().toLowerCase().contains(kw);
                        if (!matchDesc && !matchCat) continue;
                    }
                    if (startDate != null && t.getTransactionDate().isBefore(startDate)) continue;
                    if (endDate != null && t.getTransactionDate().isAfter(endDate)) continue;
                    filtered.add(t);
                }
            }

            if ("AMOUNT_DESC".equalsIgnoreCase(sortBy)) {
                filtered.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
            } else if ("AMOUNT_ASC".equalsIgnoreCase(sortBy)) {
                filtered.sort((a, b) -> a.getAmount().compareTo(b.getAmount()));
            } else if ("DATE_ASC".equalsIgnoreCase(sortBy)) {
                filtered.sort(Comparator.comparing(Transaction::getTransactionDate));
            } else {
                filtered.sort((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()));
            }
            return filtered;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT transaction_id, user_id, transaction_type, amount, category, description, " +
            "transaction_date, created_at, updated_at FROM transactions WHERE user_id = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (typeFilter != null && !typeFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND transaction_type = ? ");
            params.add(typeFilter.toUpperCase());
        }

        if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND category = ? ");
            params.add(categoryFilter);
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (LOWER(description) LIKE ? OR LOWER(category) LIKE ?) ");
            String kw = "%" + searchKeyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (startDate != null) {
            sql.append("AND transaction_date >= ? ");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append("AND transaction_date <= ? ");
            params.add(Date.valueOf(endDate));
        }

        if ("AMOUNT_DESC".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY amount DESC, transaction_date DESC ");
        } else if ("AMOUNT_ASC".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY amount ASC, transaction_date DESC ");
        } else if ("DATE_ASC".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY transaction_date ASC, transaction_id ASC ");
        } else {
            sql.append("ORDER BY transaction_date DESC, transaction_id DESC ");
        }

        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else if (p instanceof String) ps.setString(i + 1, (String) p);
                else if (p instanceof Date) ps.setDate(i + 1, (Date) p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public double getTotalAmount(int userId, String type, String monthKey) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            double sum = 0.0;
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() != userId) continue;
                    if (!t.getTransactionType().equalsIgnoreCase(type)) continue;
                    if (monthKey != null && !monthKey.isEmpty()) {
                        String m = t.getTransactionDate().toString().substring(0, 7);
                        if (!m.equals(monthKey)) continue;
                    }
                    sum += t.getAmount().doubleValue();
                }
            }
            return sum;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND transaction_type = ? "
        );
        boolean filterMonth = (monthKey != null && !monthKey.trim().isEmpty());
        if (filterMonth) {
            sql.append("AND DATE_FORMAT(transaction_date, '%Y-%m') = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            if (filterMonth) {
                ps.setString(3, monthKey.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public Map<String, Double> getCategoryExpenses(int userId, String monthKey) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            Map<String, Double> map = new LinkedHashMap<>();
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() != userId || !t.isExpense()) continue;
                    if (monthKey != null && !monthKey.isEmpty()) {
                        String m = t.getTransactionDate().toString().substring(0, 7);
                        if (!m.equals(monthKey)) continue;
                    }
                    map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0.0) + t.getAmount().doubleValue());
                }
            }
            return map;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT category, COALESCE(SUM(amount), 0) as total FROM transactions " +
            "WHERE user_id = ? AND transaction_type = 'EXPENSE' "
        );
        boolean filterMonth = (monthKey != null && !monthKey.trim().isEmpty());
        if (filterMonth) {
            sql.append("AND DATE_FORMAT(transaction_date, '%Y-%m') = ? ");
        }
        sql.append("GROUP BY category ORDER BY total DESC");

        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            if (filterMonth) {
                ps.setString(2, monthKey.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return map;
    }

    public Map<String, double[]> getMonthlyTrends(int userId, List<String> monthKeys) throws SQLException {
        Map<String, double[]> trends = new LinkedHashMap<>();
        for (String m : monthKeys) {
            trends.put(m, new double[]{0.0, 0.0});
        }

        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() != userId) continue;
                    String mkey = t.getTransactionDate().toString().substring(0, 7);
                    if (trends.containsKey(mkey)) {
                        double[] pair = trends.get(mkey);
                        if (t.isIncome()) {
                            pair[0] += t.getAmount().doubleValue();
                        } else {
                            pair[1] += t.getAmount().doubleValue();
                        }
                    }
                }
            }
            return trends;
        }

        String sql = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') as mkey, transaction_type, SUM(amount) as total " +
                     "FROM transactions WHERE user_id = ? " +
                     "GROUP BY mkey, transaction_type";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mkey = rs.getString("mkey");
                    String type = rs.getString("transaction_type");
                    double total = rs.getDouble("total");
                    if (trends.containsKey(mkey)) {
                        double[] pair = trends.get(mkey);
                        if ("INCOME".equalsIgnoreCase(type)) {
                            pair[0] = total;
                        } else if ("EXPENSE".equalsIgnoreCase(type)) {
                            pair[1] = total;
                        }
                    }
                }
            }
        }
        return trends;
    }

    public double getCategoryExpenseForMonth(int userId, String category, String monthKey) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            double sum = 0.0;
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() == userId && t.isExpense() && t.getCategory().equalsIgnoreCase(category)) {
                        String m = t.getTransactionDate().toString().substring(0, 7);
                        if (m.equals(monthKey)) {
                            sum += t.getAmount().doubleValue();
                        }
                    }
                }
            }
            return sum;
        }

        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                     "WHERE user_id = ? AND transaction_type = 'EXPENSE' AND category = ? " +
                     "AND DATE_FORMAT(transaction_date, '%Y-%m') = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public List<Transaction> findByUserId(int userId) {
        if (AuthenticationService.isDemoMode()) {
            List<Transaction> list = new ArrayList<>();
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() == userId) list.add(t);
                }
            }
            return list;
        }
        try {
            return findFiltered(userId, "ALL", "ALL", null, null, null, "DATE_DESC");
        } catch (Exception e) {
            List<Transaction> list = new ArrayList<>();
            synchronized (memoryStore) {
                for (Transaction t : memoryStore) {
                    if (t.getUserId() == userId) list.add(t);
                }
            }
            return list;
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setUserId(rs.getInt("user_id"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setCategory(rs.getString("category"));
        tx.setDescription(rs.getString("description"));

        Date dt = rs.getDate("transaction_date");
        if (dt != null) {
            tx.setTransactionDate(dt.toLocalDate());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            tx.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            tx.setUpdatedAt(updated.toLocalDateTime());
        }
        return tx;
    }
}
