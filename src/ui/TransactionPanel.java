package ui;

import model.Transaction;
import service.AuthenticationService;
import service.TransactionService;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import ui.components.TransactionTablePanel;
import util.CurrencyFormatter;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Screen 5: Dedicated Transaction Management Screen built with Swing & AWT.
 * Provides search, multi-criteria filtering, sorting, CSV export, and full CRUD operations.
 */
public class TransactionPanel extends JPanel {
    private final Frame parentFrame;
    private final TransactionService transactionService;

    private JTextField txtSearch;
    private JComboBox<String> comboType;
    private JComboBox<String> comboCategory;
    private JComboBox<String> comboSort;

    private TransactionTablePanel tablePanel;
    private JLabel lblStats;
    private int resultCount = 0;
    private double totalAmount = 0.0;

    public TransactionPanel(Frame parentFrame, TransactionService service) {
        this.parentFrame = parentFrame;
        this.transactionService = service;

        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // -------------------------------------------------------------
        // TOP FILTER TOOLBAR
        // -------------------------------------------------------------
        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setBackground(ThemeColors.SURFACE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterControls.setBackground(ThemeColors.SURFACE);

        // Search Input
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(FontManager.SMALL_BUTTON);
        lblSearch.setForeground(ThemeColors.TEXT_MUTED);
        txtSearch = new JTextField(12);
        txtSearch.setFont(FontManager.FORM_INPUT);

        // Type Filter
        JLabel lblType = new JLabel("Type:");
        lblType.setFont(FontManager.SMALL_BUTTON);
        lblType.setForeground(ThemeColors.TEXT_MUTED);
        comboType = new JComboBox<>(new String[]{"ALL", "INCOME", "EXPENSE"});
        comboType.setFont(FontManager.FORM_INPUT);

        // Category Filter
        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(FontManager.SMALL_BUTTON);
        lblCat.setForeground(ThemeColors.TEXT_MUTED);
        String[] cats = {
            "ALL", "Salary", "Food", "Travel", "Education", "Shopping",
            "Bills", "Entertainment", "Healthcare", "Freelance", "Allowance", "Business", "Other"
        };
        comboCategory = new JComboBox<>(cats);
        comboCategory.setFont(FontManager.FORM_INPUT);

        // Sort Filter
        JLabel lblSort = new JLabel("Sort By:");
        lblSort.setFont(FontManager.SMALL_BUTTON);
        lblSort.setForeground(ThemeColors.TEXT_MUTED);
        comboSort = new JComboBox<>(new String[]{"Newest First", "Oldest First", "Highest Amount", "Lowest Amount"});
        comboSort.setFont(FontManager.FORM_INPUT);

        // Apply / Clear buttons
        CustomButton btnApply = new CustomButton("Filter", CustomButton.Style.PRIMARY);
        btnApply.setCustomFont(FontManager.SMALL_BUTTON);
        btnApply.setPreferredSize(new Dimension(68, 28));
        btnApply.addActionListener(e -> applyFilters());

        CustomButton btnClear = new CustomButton("Clear", CustomButton.Style.SECONDARY);
        btnClear.setCustomFont(FontManager.SMALL_BUTTON);
        btnClear.setPreferredSize(new Dimension(62, 28));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            comboType.setSelectedIndex(0);
            comboCategory.setSelectedIndex(0);
            comboSort.setSelectedIndex(0);
            applyFilters();
        });

        filterControls.add(lblSearch);
        filterControls.add(txtSearch);
        filterControls.add(lblType);
        filterControls.add(comboType);
        filterControls.add(lblCat);
        filterControls.add(comboCategory);
        filterControls.add(lblSort);
        filterControls.add(comboSort);
        filterControls.add(btnApply);
        filterControls.add(btnClear);

        // Action Buttons on Right (Add New, Export CSV)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rightActions.setBackground(ThemeColors.SURFACE);

        CustomButton btnExport = new CustomButton("Export CSV", CustomButton.Style.OUTLINE);
        btnExport.setCustomFont(FontManager.SMALL_BUTTON);
        btnExport.setPreferredSize(new Dimension(100, 28));
        btnExport.addActionListener(e -> exportToCsv());

        CustomButton btnAdd = new CustomButton("+ Add Transaction", CustomButton.Style.SUCCESS);
        btnAdd.setCustomFont(FontManager.SMALL_BUTTON);
        btnAdd.setPreferredSize(new Dimension(140, 28));
        btnAdd.addActionListener(e -> openAddDialog());

        rightActions.add(btnExport);
        rightActions.add(btnAdd);

        filterBar.add(filterControls, BorderLayout.CENTER);
        filterBar.add(rightActions, BorderLayout.EAST);

        add(filterBar, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // STATS SUMMARY STRIP
        // -------------------------------------------------------------
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        statsPanel.setBackground(ThemeColors.BG_MAIN);
        lblStats = new JLabel("Showing 0 records matching filter criteria | Total Filtered Amount: ₹0.00");
        lblStats.setFont(FontManager.SMALL);
        lblStats.setForeground(ThemeColors.TEXT_MUTED);
        statsPanel.add(lblStats);

        // Container for Table + Stats
        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setBackground(ThemeColors.BG_MAIN);
        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // TRANSACTIONS TABLE (Swing JTable)
        // -------------------------------------------------------------
        tablePanel = new TransactionTablePanel();
        tablePanel.setListener(new TransactionTablePanel.TransactionActionListener() {
            @Override
            public void onEdit(Transaction tx) {
                openEditDialog(tx);
            }

            @Override
            public void onDelete(Transaction tx) {
                confirmDelete(tx);
            }
        });
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        applyFilters();
    }

    private void applyFilters() {
        int userId = AuthenticationService.getCurrentUser() != null ?
                AuthenticationService.getCurrentUser().getUserId() : 1;

        String kw = txtSearch.getText().trim();
        String type = (String) comboType.getSelectedItem();
        String cat = (String) comboCategory.getSelectedItem();
        String sortOption = (String) comboSort.getSelectedItem();

        String sortBy = "DATE_DESC";
        if ("Oldest First".equals(sortOption)) sortBy = "DATE_ASC";
        else if ("Highest Amount".equals(sortOption)) sortBy = "AMOUNT_DESC";
        else if ("Lowest Amount".equals(sortOption)) sortBy = "AMOUNT_ASC";

        final String finalSortBy = sortBy;

        new Thread(() -> {
            try {
                List<Transaction> list = transactionService.getFilteredTransactions(
                    userId, type, cat, kw, null, null, finalSortBy
                );

                double total = 0.0;
                for (Transaction tx : list) {
                    total += tx.getAmount().doubleValue();
                }
                final double sum = total;
                final int count = list.size();

                SwingUtilities.invokeLater(() -> {
                    resultCount = count;
                    totalAmount = sum;
                    tablePanel.setTransactions(list);
                    lblStats.setText(String.format("Showing %d records matching filter criteria | Total Filtered Amount: %s",
                            resultCount, CurrencyFormatter.format(totalAmount)));
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openAddDialog() {
        TransactionFormDialog dlg = new TransactionFormDialog(parentFrame, "EXPENSE", null, transactionService, () -> {
            applyFilters();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog(Transaction tx) {
        TransactionFormDialog dlg = new TransactionFormDialog(parentFrame, tx.getTransactionType(), tx, transactionService, () -> {
            applyFilters();
        });
        dlg.setVisible(true);
    }

    private void confirmDelete(Transaction tx) {
        boolean confirmed = ConfirmationDialog.showConfirm(
            parentFrame,
            "Delete Transaction",
            "Are you sure you want to delete this " + tx.getCategory() + " transaction of ₹" + tx.getAmount() + "?",
            "Delete",
            true
        );
        if (confirmed) {
            new Thread(() -> {
                try {
                    int userId = AuthenticationService.getCurrentUser() != null ?
                            AuthenticationService.getCurrentUser().getUserId() : 1;
                    transactionService.deleteTransaction(tx.getTransactionId(), userId);
                    SwingUtilities.invokeLater(this::applyFilters);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("transactions_export.csv"));
        int res = chooser.showSaveDialog(parentFrame);

        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            int userId = AuthenticationService.getCurrentUser() != null ?
                    AuthenticationService.getCurrentUser().getUserId() : 1;
            new Thread(() -> {
                try {
                    List<Transaction> list = transactionService.getFilteredTransactions(
                        userId, "ALL", "ALL", null, null, null, "DATE_DESC"
                    );
                    boolean ok = util.CsvExporter.exportTransactions(target, list);
                    SwingUtilities.invokeLater(() -> {
                        if (ok) {
                            JOptionPane.showMessageDialog(parentFrame,
                                "Transactions exported successfully to " + target.getName(),
                                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(parentFrame,
                                "Failed to export transactions to CSV.",
                                "Export Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
