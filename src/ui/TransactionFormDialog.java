package ui;

import model.Transaction;
import service.AuthenticationService;
import service.TransactionService;
import ui.components.CustomButton;
import util.FontManager;
import util.InputValidator;
import util.ThemeColors;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Swing modal JDialog for recording new transactions or editing existing records.
 * Uses AWT GridBagLayout and Swing form controls.
 */
public class TransactionFormDialog extends JDialog {
    public interface FormCallback {
        void onSuccess();
    }

    private final TransactionService transactionService;
    private final FormCallback callback;
    private final Transaction existingTransaction;

    private JComboBox<String> comboType;
    private JTextField txtAmount;
    private JComboBox<String> comboCategory;
    private JTextField txtDate;
    private JTextField txtDescription;
    private JLabel lblError;

    private static final String[] INCOME_CATEGORIES = {
        "Salary", "Allowance", "Freelance", "Scholarship", "Business", "Other"
    };

    private static final String[] EXPENSE_CATEGORIES = {
        "Food", "Travel", "Education", "Shopping", "Bills", "Entertainment", "Healthcare", "Other"
    };

    public TransactionFormDialog(Frame parent, String defaultType, Transaction txToEdit,
                                 TransactionService service, FormCallback callback) {
        super(parent, (txToEdit == null ? "Add Transaction" : "Edit Transaction"), true);
        this.transactionService = service;
        this.callback = callback;
        this.existingTransaction = txToEdit;

        setLayout(new BorderLayout());
        setResizable(false);
        setBackground(ThemeColors.SURFACE);

        // Header Title Banner
        JPanel headerPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(450, 56);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY_LIGHT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeColors.PRIMARY);
                g2.setFont(FontManager.SECTION_TITLE);
                g2.drawString(existingTransaction == null ? "Record New Transaction" : "Update Transaction Details", 20, 36);
                g2.setColor(ThemeColors.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields in AWT GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeColors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Error message label
        lblError = new JLabel("");
        lblError.setForeground(ThemeColors.EXPENSE);
        lblError.setFont(FontManager.SMALL);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(lblError, gbc);

        // 1. Transaction Type
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lblType = new JLabel("Transaction Type:");
        lblType.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblType, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        comboType = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        comboType.setFont(FontManager.FORM_INPUT);
        if (existingTransaction != null) {
            comboType.setSelectedItem(existingTransaction.getTransactionType());
        } else if (defaultType != null) {
            comboType.setSelectedItem(defaultType.toUpperCase());
        }
        formPanel.add(comboType, gbc);

        // 2. Amount
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblAmount = new JLabel("Amount (₹):");
        lblAmount.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblAmount, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtAmount = new JTextField(20);
        txtAmount.setFont(FontManager.FORM_INPUT);
        if (existingTransaction != null) {
            txtAmount.setText(existingTransaction.getAmount().toPlainString());
        }
        formPanel.add(txtAmount, gbc);

        // 3. Category
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblCat, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        comboCategory = new JComboBox<>();
        comboCategory.setFont(FontManager.FORM_INPUT);
        populateCategories();
        if (existingTransaction != null) {
            comboCategory.setSelectedItem(existingTransaction.getCategory());
        }
        formPanel.add(comboCategory, gbc);

        comboType.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                populateCategories();
            }
        });

        // 4. Date (YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        lblDate.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblDate, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDate = new JTextField(20);
        txtDate.setFont(FontManager.FORM_INPUT);
        if (existingTransaction != null) {
            txtDate.setText(existingTransaction.getTransactionDate().toString());
        } else {
            txtDate.setText(LocalDate.now().toString());
        }
        formPanel.add(txtDate, gbc);

        // 5. Description
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDescription = new JTextField(20);
        txtDescription.setFont(FontManager.FORM_INPUT);
        if (existingTransaction != null) {
            txtDescription.setText(existingTransaction.getDescription());
        }
        formPanel.add(txtDescription, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(ThemeColors.SURFACE);

        CustomButton btnCancel = new CustomButton("Cancel", CustomButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> setVisible(false));

        CustomButton btnSave = new CustomButton(
            existingTransaction == null ? "Save Transaction" : "Update Transaction",
            CustomButton.Style.PRIMARY
        );
        btnSave.addActionListener(e -> handleSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void populateCategories() {
        String selectedType = (String) comboType.getSelectedItem();
        comboCategory.removeAllItems();
        if ("INCOME".equalsIgnoreCase(selectedType)) {
            for (String cat : INCOME_CATEGORIES) {
                comboCategory.addItem(cat);
            }
        } else {
            for (String cat : EXPENSE_CATEGORIES) {
                comboCategory.addItem(cat);
            }
        }
    }

    private void handleSave() {
        lblError.setText("");
        String type = (String) comboType.getSelectedItem();
        String amtStr = txtAmount.getText().trim();
        String category = (String) comboCategory.getSelectedItem();
        String dateStr = txtDate.getText().trim();
        String desc = txtDescription.getText().trim();

        if (!InputValidator.isPositiveAmount(amtStr)) {
            lblError.setText("Please enter a valid positive amount.");
            return;
        }

        BigDecimal amount = InputValidator.parseAmount(amtStr);

        if (!InputValidator.isValidDate(dateStr)) {
            lblError.setText("Date must be in valid YYYY-MM-DD format.");
            return;
        }

        LocalDate date = InputValidator.parseDate(dateStr);

        if (!InputValidator.isNotEmpty(desc)) {
            lblError.setText("Description cannot be empty.");
            return;
        }

        int userId = AuthenticationService.getCurrentUser() != null ?
                AuthenticationService.getCurrentUser().getUserId() : 1;

        // Perform DB operation in background thread
        new Thread(() -> {
            try {
                if (existingTransaction == null) {
                    transactionService.addTransaction(userId, type, amount, category, desc, date);
                } else {
                    transactionService.updateTransaction(existingTransaction.getTransactionId(),
                            userId, type, amount, category, desc, date);
                }

                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblError.setText("Error saving: " + ex.getMessage());
                });
            }
        }).start();
    }
}
