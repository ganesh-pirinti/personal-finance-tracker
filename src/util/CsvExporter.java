package util;

import model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility to export transactions and financial summaries to standard CSV files.
 */
public final class CsvExporter {
    private CsvExporter() {}

    public static boolean exportTransactions(File targetFile, List<Transaction> transactions) {
        if (targetFile == null || transactions == null) return false;
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {

            // Header
            writer.println("Transaction ID,Date,Type,Category,Description,Amount (INR)");

            for (Transaction tx : transactions) {
                writer.printf("%d,%s,%s,\"%s\",\"%s\",%.2f%n",
                    tx.getTransactionId(),
                    DateUtils.formatIso(tx.getTransactionDate()),
                    tx.getTransactionType(),
                    escapeCsv(tx.getCategory()),
                    escapeCsv(tx.getDescription()),
                    tx.getAmount().doubleValue()
                );
            }
            writer.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}
