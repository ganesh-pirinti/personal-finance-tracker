package ui;

import model.Budget;
import service.AuthenticationService;
import service.BudgetService;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import ui.components.ProgressBarComponent;
import util.DateUtils;
import util.FontManager;
import util.InputValidator;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Screen 6: Monthly Budget Management and Spending Progress Tracking built with Swing & AWT.
 */
public class BudgetPanel extends JPanel {
    private final Frame parentFrame;
    private final BudgetService budgetService;

    private JComboBox<String> comboMonth;
    private JPanel budgetCardsContainer;
    private JScrollPane scrollPane;

    public BudgetPanel(Frame parentFrame, BudgetService service) {
        this.parentFrame = parentFrame;
        this.budgetService = service;

        setLayout(new BorderLayout(0, 14));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // -------------------------------------------------------------
        // TOP CONTROL BAR
        // -------------------------------------------------------------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeColors.SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        leftControls.setBackground(ThemeColors.SURFACE);

        JLabel lblMonth = new JLabel("Select Month:");
        lblMonth.setFont(FontManager.SMALL_BUTTON);
        lblMonth.setForeground(ThemeColors.TEXT_MUTED);

        comboMonth = new JComboBox<>();
        comboMonth.setFont(FontManager.FORM_INPUT);
        List<String> months = DateUtils.getLastMonths(6);
        for (String m : months) {
            comboMonth.addItem(m);
        }
        comboMonth.setSelectedItem(DateUtils.getCurrentMonthKey());
        comboMonth.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                refreshData();
            }
        });

        leftControls.add(lblMonth);
        leftControls.add(comboMonth);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        rightControls.setBackground(ThemeColors.SURFACE);

        CustomButton btnSetBudget = new CustomButton("+ Set Category Budget", CustomButton.Style.PRIMARY);
        btnSetBudget.setCustomFont(FontManager.SMALL_BUTTON);
        btnSetBudget.setPreferredSize(new Dimension(170, 30));
        btnSetBudget.addActionListener(e -> openSetBudgetDialog());

        rightControls.add(btnSetBudget);

        topBar.add(leftControls, BorderLayout.CENTER);
        topBar.add(rightControls, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // BUDGET CARDS CONTAINER (Scrollable)
        // -------------------------------------------------------------
        budgetCardsContainer = new JPanel();
        budgetCardsContainer.setBackground(ThemeColors.BG_MAIN);

        scrollPane = new JScrollPane(budgetCardsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        int userId = AuthenticationService.getCurrentUser() != null ?
                AuthenticationService.getCurrentUser().getUserId() : 1;
        String selectedMonth = (String) comboMonth.getSelectedItem();

        new Thread(() -> {
            try {
                List<Budget> budgets = budgetService.getBudgetsForMonth(userId, selectedMonth);

                SwingUtilities.invokeLater(() -> {
                    budgetCardsContainer.removeAll();

                    if (budgets.isEmpty()) {
                        budgetCardsContainer.setLayout(new BorderLayout());
                        JLabel lblEmpty = new JLabel(
                            "<html><center>No spending budgets defined for " + DateUtils.formatMonthKeyDisplay(selectedMonth) +
                            ".<br><span style='color:#94A3B8; font-size:11px;'>Click '+ Set Category Budget' to plan your monthly expenses.</span></center></html>",
                            SwingConstants.CENTER
                        );
                        lblEmpty.setFont(FontManager.BODY);
                        lblEmpty.setForeground(ThemeColors.TEXT_MUTED);
                        budgetCardsContainer.add(lblEmpty, BorderLayout.CENTER);
                    } else {
                        budgetCardsContainer.setLayout(new GridLayout(Math.max(budgets.size(), 1), 1, 0, 12));
                        for (Budget b : budgets) {
                            budgetCardsContainer.add(createBudgetCard(b));
                        }
                    }

                    budgetCardsContainer.revalidate();
                    budgetCardsContainer.repaint();
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel createBudgetCard(Budget b) {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SURFACE);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(650, 88));
        card.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        ProgressBarComponent bar = new ProgressBarComponent(b.getCategory(), b.getSpentAmount(), b.getBudgetLimit().doubleValue());
        card.add(bar, BorderLayout.CENTER);

        // Actions panel (Delete button)
        JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 20));
        actPanel.setOpaque(false);

        CustomButton btnDelete = new CustomButton("Remove", CustomButton.Style.DANGER);
        btnDelete.setCustomFont(FontManager.SMALL_BUTTON);
        btnDelete.setPreferredSize(new Dimension(72, 28));
        btnDelete.addActionListener(e -> {
            boolean confirm = ConfirmationDialog.showConfirm(
                parentFrame,
                "Remove Budget",
                "Remove budget limit for " + b.getCategory() + "?",
                "Remove",
                true
            );
            if (confirm) {
                new Thread(() -> {
                    try {
                        int userId = AuthenticationService.getCurrentUser() != null ?
                                AuthenticationService.getCurrentUser().getUserId() : 1;
                        budgetService.deleteBudget(b.getBudgetId(), userId);
                        SwingUtilities.invokeLater(this::refreshData);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        actPanel.add(btnDelete);
        card.add(actPanel, BorderLayout.EAST);

        return card;
    }

    private void openSetBudgetDialog() {
        JDialog dlg = new JDialog(parentFrame, "Set Monthly Category Budget", true);
        dlg.setLayout(new BorderLayout());
        dlg.setBackground(ThemeColors.SURFACE);
        dlg.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ThemeColors.SURFACE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblErr = new JLabel("");
        lblErr.setForeground(ThemeColors.EXPENSE);
        lblErr.setFont(FontManager.SMALL);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblErr, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.35;
        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(FontManager.FORM_LABEL);
        form.add(lblCat, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        String[] cats = {"Food", "Travel", "Education", "Shopping", "Bills", "Entertainment", "Healthcare", "Other"};
        JComboBox<String> cCat = new JComboBox<>(cats);
        cCat.setFont(FontManager.FORM_INPUT);
        form.add(cCat, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        JLabel lblLimit = new JLabel("Budget Limit (₹):");
        lblLimit.setFont(FontManager.FORM_LABEL);
        form.add(lblLimit, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        JTextField txtLimit = new JTextField(15);
        txtLimit.setFont(FontManager.FORM_INPUT);
        form.add(txtLimit, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(ThemeColors.SURFACE);
        CustomButton btnCancel = new CustomButton("Cancel", CustomButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> dlg.setVisible(false));

        CustomButton btnSave = new CustomButton("Save Budget", CustomButton.Style.PRIMARY);
        btnSave.addActionListener(e -> {
            lblErr.setText("");
            String cat = (String) cCat.getSelectedItem();
            String amtStr = txtLimit.getText().trim();
            if (!InputValidator.isPositiveAmount(amtStr)) {
                lblErr.setText("Enter a valid positive budget amount.");
                return;
            }
            BigDecimal limit = InputValidator.parseAmount(amtStr);
            int userId = AuthenticationService.getCurrentUser() != null ?
                    AuthenticationService.getCurrentUser().getUserId() : 1;
            String month = (String) comboMonth.getSelectedItem();

            new Thread(() -> {
                try {
                    budgetService.setBudget(userId, cat, month, limit);
                    SwingUtilities.invokeLater(() -> {
                        dlg.setVisible(false);
                        refreshData();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblErr.setText("Error: " + ex.getMessage());
                    });
                }
            }).start();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setVisible(true);
    }
}
