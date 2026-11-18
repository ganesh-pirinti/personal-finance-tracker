package ui;

import model.FinancialSummary;
import model.Transaction;
import service.AuthenticationService;
import service.TransactionService;
import ui.components.ChartCanvas;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import ui.components.DonutChartCanvas;
import ui.components.MetricCard;
import ui.components.TransactionTablePanel;
import util.DateUtils;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.List;

/**
 * Screen 3: Main Financial Dashboard built with Swing & AWT.
 * Displays Income/Expense/Balance metric cards, 6-month trend chart,
 * category donut chart, and recent transaction records in a Swing JTable.
 */
public class DashboardPanel extends JPanel {
    private final Frame parentFrame;
    private final TransactionService transactionService;
    private final NavigationPanel.NavigationListener navListener;

    private MetricCard cardIncome;
    private MetricCard cardExpense;
    private MetricCard cardBalance;

    private ChartCanvas trendChart;
    private DonutChartCanvas donutChart;
    private TransactionTablePanel recentTable;

    public DashboardPanel(Frame parentFrame, TransactionService service, NavigationPanel.NavigationListener navListener) {
        this.parentFrame = parentFrame;
        this.transactionService = service;
        this.navListener = navListener;

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);

        // Responsive content panel tracking viewport width to prevent horizontal overflow
        ScrollableContentPanel content = new ScrollableContentPanel(new BorderLayout(0, 16));
        content.setBackground(ThemeColors.BG_MAIN);
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // -------------------------------------------------------------
        // ROW 1: 3 METRIC CARDS
        // -------------------------------------------------------------
        JPanel metricRow = new JPanel(new GridLayout(1, 3, 16, 0));
        metricRow.setBackground(ThemeColors.BG_MAIN);

        cardIncome = new MetricCard("Total Income", 0.0, "Current Month", MetricCard.Type.INCOME);
        cardExpense = new MetricCard("Total Expenses", 0.0, "Current Month", MetricCard.Type.EXPENSE);
        cardBalance = new MetricCard("Available Balance", 0.0, "Net Balance", MetricCard.Type.BALANCE);

        metricRow.add(cardIncome);
        metricRow.add(cardExpense);
        metricRow.add(cardBalance);

        content.add(metricRow, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // ROW 2: CHARTS (Trend Bar Chart + Category Donut Chart)
        // -------------------------------------------------------------
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 16));
        centerWrapper.setBackground(ThemeColors.BG_MAIN);

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setBackground(ThemeColors.BG_MAIN);

        trendChart = new ChartCanvas();
        donutChart = new DonutChartCanvas();

        chartsRow.add(trendChart);
        chartsRow.add(donutChart);
        centerWrapper.add(chartsRow, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // ROW 3: RECENT TRANSACTIONS TABLE
        // -------------------------------------------------------------
        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setBackground(ThemeColors.BG_MAIN);

        // Table Header Banner with "Recent Transactions" and "View All"
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setBackground(ThemeColors.BG_MAIN);

        JPanel lblTitlePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(220, 32); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(FontManager.CARD_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString("Recent Transactions", 4, 22);
                g2.dispose();
            }
        };
        lblTitlePanel.setOpaque(false);
        tableHeaderBar.add(lblTitlePanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setBackground(ThemeColors.BG_MAIN);
        CustomButton btnViewAll = new CustomButton("View All Transactions →", CustomButton.Style.OUTLINE);
        btnViewAll.setCustomFont(FontManager.SMALL_BUTTON);
        btnViewAll.setPreferredSize(new Dimension(170, 30));
        btnViewAll.addActionListener(e -> {
            if (navListener != null) {
                navListener.onNavigate(NavigationPanel.NAV_TRANSACTIONS);
            }
        });
        btnPanel.add(btnViewAll);
        tableHeaderBar.add(btnPanel, BorderLayout.EAST);

        tableSection.add(tableHeaderBar, BorderLayout.NORTH);

        // Table component with responsive width
        recentTable = new TransactionTablePanel();
        recentTable.setPreferredSize(new Dimension(100, 240));
        recentTable.setListener(new TransactionTablePanel.TransactionActionListener() {
            @Override
            public void onEdit(Transaction tx) {
                openEditDialog(tx);
            }

            @Override
            public void onDelete(Transaction tx) {
                confirmDelete(tx);
            }
        });
        recentTable.setEmptyActionCallback(new TransactionTablePanel.EmptyActionCallback() {
            @Override
            public void onAddIncome() {
                if (navListener != null) {
                    navListener.onNavigate(NavigationPanel.NAV_ADD_INCOME);
                }
            }

            @Override
            public void onAddExpense() {
                if (navListener != null) {
                    navListener.onNavigate(NavigationPanel.NAV_ADD_EXPENSE);
                }
            }
        });
        tableSection.add(recentTable, BorderLayout.CENTER);

        centerWrapper.add(tableSection, BorderLayout.CENTER);
        content.add(centerWrapper, BorderLayout.CENTER);

        // Swing JScrollPane with zero horizontal scrollbar and responsive viewport width tracking
        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private static class ScrollableContentPanel extends JPanel implements javax.swing.Scrollable {
        public ScrollableContentPanel(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // Enforces child components to stay strictly within viewport width
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public void refreshData() {
        int userId = AuthenticationService.getCurrentUser() != null ?
                AuthenticationService.getCurrentUser().getUserId() : 1;
        String currentMonth = DateUtils.getCurrentMonthKey();

        new Thread(() -> {
            try {
                FinancialSummary summary = transactionService.getSummary(userId, currentMonth);
                List<Transaction> recents = transactionService.getRecentTransactions(userId, 8);

                SwingUtilities.invokeLater(() -> {
                    // Update Metric Cards
                    cardIncome.updateData(summary.getTotalIncome(), "Month of " + DateUtils.formatMonthKeyDisplay(currentMonth));
                    cardExpense.updateData(summary.getTotalExpense(), "Month of " + DateUtils.formatMonthKeyDisplay(currentMonth));
                    cardBalance.updateData(summary.getAvailableBalance(), "Income − Expenses");

                    // Update Charts
                    trendChart.setData(summary.getMonthlyIncomeTrend(), summary.getMonthlyExpenseTrend());
                    donutChart.setData(summary.getCategoryExpenses());

                    // Update Recent Transactions Table
                    recentTable.setTransactions(recents);
                });
            } catch (SQLException e) {
                System.err.println("Dashboard refresh SQL error: " + e.getMessage());
            }
        }).start();
    }

    private void openEditDialog(Transaction tx) {
        TransactionFormDialog dlg = new TransactionFormDialog(parentFrame, tx.getTransactionType(), tx, transactionService, () -> {
            refreshData();
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
                    SwingUtilities.invokeLater(this::refreshData);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
