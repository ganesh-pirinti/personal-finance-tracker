package service;

import dao.TransactionDAO;
import model.FinancialSummary;
import model.Transaction;
import util.CsvExporter;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for generating comprehensive financial reports and exporting data.
 */
public class ReportService {
    private final TransactionDAO transactionDAO;
    private final TransactionService transactionService;

    public ReportService() {
        this.transactionDAO = new TransactionDAO();
        this.transactionService = new TransactionService(transactionDAO);
    }

    public FinancialSummary generateReport(int userId, String monthKey) throws SQLException {
        return transactionService.getSummary(userId, monthKey);
    }

    public boolean exportUserTransactionsToCsv(int userId, File targetFile) throws SQLException {
        List<Transaction> transactions = transactionDAO.findFiltered(userId, "ALL", "ALL", null, null, null, "DATE_DESC");
        return CsvExporter.exportTransactions(targetFile, transactions);
    }
}
