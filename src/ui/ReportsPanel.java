package ui;

import model.FinancialSummary;
import service.AuthenticationService;
import service.ReportService;
import ui.components.CustomButton;
import ui.components.MetricCard;
import util.CurrencyFormatter;
import util.DateUtils;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Screen 7: Comprehensive Financial Reports and Analytics built with Swing & AWT.
 */
public class ReportsPanel extends JPanel {
    private final Frame parentFrame;
    private final ReportService reportService;

    private JComboBox<String> comboPeriod;
    private MetricCard cardIncome;
    private MetricCard cardExpense;
    private MetricCard cardBalance;

    private JPanel analyticsPanel;
    private JPanel breakdownTablePanel;

    private FinancialSummary currentSummary = new FinancialSummary();

    public ReportsPanel(Frame parentFrame, ReportService service) {
        this.parentFrame = parentFrame;
        this.reportService = service;

        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // -------------------------------------------------------------
        // TOP CONTROL TOOLBAR
        // -------------------------------------------------------------
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(ThemeColors.SURFACE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        leftControls.setBackground(ThemeColors.SURFACE);

        JLabel lblPeriod = new JLabel("Report Scope:");
        lblPeriod.setFont(FontManager.SMALL_BUTTON);
        lblPeriod.setForeground(ThemeColors.TEXT_MUTED);

        comboPeriod = new JComboBox<>();
        comboPeriod.setFont(FontManager.FORM_INPUT);
        comboPeriod.addItem("All Time");
        List<String> months = DateUtils.getLastMonths(6);
        for (String m : months) {
            comboPeriod.addItem(m);
        }
        comboPeriod.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                refreshData();
            }
        });

        CustomButton btnRefresh = new CustomButton("Refresh", CustomButton.Style.SECONDARY);
        btnRefresh.setCustomFont(FontManager.SMALL_BUTTON);
        btnRefresh.setPreferredSize(new Dimension(80, 28));
        btnRefresh.addActionListener(e -> refreshData());

        leftControls.add(lblPeriod);
        leftControls.add(comboPeriod);
        leftControls.add(btnRefresh);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        rightControls.setBackground(ThemeColors.SURFACE);

        CustomButton btnExport = new CustomButton("Export Report CSV", CustomButton.Style.OUTLINE);
        btnExport.setCustomFont(FontManager.SMALL_BUTTON);
        btnExport.setPreferredSize(new Dimension(150, 28));
        btnExport.addActionListener(e -> exportReportCsv());

        rightControls.add(btnExport);

        toolbar.add(leftControls, BorderLayout.CENTER);
        toolbar.add(rightControls, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // SCROLLABLE REPORT CONTENT
        // -------------------------------------------------------------
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(ThemeColors.BG_MAIN);

        // Metric Cards Row
        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        metricsRow.setBackground(ThemeColors.BG_MAIN);

        cardIncome = new MetricCard("Total Income", 0.0, "Selected Scope", MetricCard.Type.INCOME);
        cardExpense = new MetricCard("Total Expenses", 0.0, "Selected Scope", MetricCard.Type.EXPENSE);
        cardBalance = new MetricCard("Net Balance", 0.0, "Income − Expenses", MetricCard.Type.BALANCE);

        metricsRow.add(cardIncome);
        metricsRow.add(cardExpense);
        metricsRow.add(cardBalance);

        content.add(metricsRow, BorderLayout.NORTH);

        // Middle Section: Analytical Key Metrics Card
        analyticsPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(680, 110); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SURFACE);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);

                int colW = (w - 40) / 3;

                // Col 1: Highest Spending Category
                drawMetricBox(g2, 20, 20, colW, "Highest Spending Category",
                        currentSummary.getHighestCategory(),
                        CurrencyFormatter.format(currentSummary.getHighestCategoryAmount()),
                        ThemeColors.EXPENSE);

                // Col 2: Average Expense per Category
                drawMetricBox(g2, 20 + colW, 20, colW, "Average Category Expense",
                        CurrencyFormatter.format(currentSummary.getAverageExpense()),
                        "Across active categories",
                        ThemeColors.PRIMARY);

                // Col 3: Net Savings Rate
                double sRate = currentSummary.getSavingsRate();
                drawMetricBox(g2, 20 + (colW * 2), 20, colW, "Net Savings Ratio",
                        String.format("%.1f%%", sRate),
                        sRate >= 0 ? "Positive Net Retention" : "Deficit Spending",
                        sRate >= 20 ? ThemeColors.INCOME : (sRate >= 0 ? ThemeColors.WARNING : ThemeColors.EXPENSE));

                g2.dispose();
            }

            private void drawMetricBox(Graphics2D g2, int x, int y, int width, String title, String val, String sub, Color col) {
                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.drawString(title, x, y + 10);

                g2.setFont(FontManager.SECTION_TITLE);
                g2.setColor(col);
                g2.drawString(val, x, y + 42);

                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.TEXT_LIGHT);
                g2.drawString(sub, x, y + 68);
            }
        };
        analyticsPanel.setOpaque(false);

        JPanel middleWrapper = new JPanel(new BorderLayout(0, 14));
        middleWrapper.setBackground(ThemeColors.BG_MAIN);
        middleWrapper.add(analyticsPanel, BorderLayout.NORTH);

        // Category Breakdown Table Panel
        breakdownTablePanel = new JPanel(new BorderLayout());
        breakdownTablePanel.setBackground(ThemeColors.SURFACE);
        breakdownTablePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1));
        middleWrapper.add(breakdownTablePanel, BorderLayout.CENTER);

        content.add(middleWrapper, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        int userId = AuthenticationService.getCurrentUser() != null ?
                AuthenticationService.getCurrentUser().getUserId() : 1;
        String sel = (String) comboPeriod.getSelectedItem();
        String monthKey = "All Time".equals(sel) ? null : sel;

        new Thread(() -> {
            try {
                FinancialSummary summary = reportService.generateReport(userId, monthKey);

                SwingUtilities.invokeLater(() -> {
                    currentSummary = summary;
                    cardIncome.updateData(currentSummary.getTotalIncome(), "Scope: " + (monthKey != null ? monthKey : "All Time"));
                    cardExpense.updateData(currentSummary.getTotalExpense(), "Scope: " + (monthKey != null ? monthKey : "All Time"));
                    cardBalance.updateData(currentSummary.getAvailableBalance(), "Available Balance");

                    analyticsPanel.repaint();
                    refreshBreakdownTable();
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void refreshBreakdownTable() {
        breakdownTablePanel.removeAll();

        Map<String, Double> expenses = currentSummary.getCategoryExpenses();
        double totalExp = currentSummary.getTotalExpense();

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() { return new Dimension(650, 36); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(248, 250, 252));
                g2.fillRect(0, 0, w, h);
                g2.setColor(ThemeColors.BORDER);
                g2.drawLine(0, h - 1, w, h - 1);

                g2.setFont(FontManager.TABLE_HEADER);
                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.drawString("CATEGORY BREAKDOWN", 20, 22);
                g2.drawString("AMOUNT SPENT", 350, 22);
                g2.drawString("SHARE (%)", w - 120, 22);
                g2.dispose();
            }
        };
        breakdownTablePanel.add(header, BorderLayout.NORTH);

        if (expenses.isEmpty()) {
            JLabel lblEmpty = new JLabel("No category expense records found for this scope.", SwingConstants.CENTER);
            lblEmpty.setPreferredSize(new Dimension(600, 100));
            lblEmpty.setFont(FontManager.BODY);
            lblEmpty.setForeground(ThemeColors.TEXT_LIGHT);
            breakdownTablePanel.add(lblEmpty, BorderLayout.CENTER);
        } else {
            JPanel rows = new JPanel(new GridLayout(expenses.size(), 1, 0, 1));
            rows.setBackground(ThemeColors.SURFACE);

            int idx = 0;
            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                String cat = entry.getKey();
                double amt = entry.getValue();
                double pct = (totalExp > 0) ? (amt / totalExp) * 100.0 : 0.0;
                boolean isAlt = (idx % 2 == 1);

                JPanel rowPanel = new JPanel() {
                    @Override
                    public Dimension getPreferredSize() { return new Dimension(650, 34); }
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = getWidth();
                        int h = getHeight();

                        g2.setColor(isAlt ? new Color(248, 250, 252) : ThemeColors.SURFACE);
                        g2.fillRect(0, 0, w, h);

                        g2.setFont(FontManager.BODY_BOLD);
                        g2.setColor(ThemeColors.TEXT_MAIN);
                        g2.drawString(cat, 20, 22);

                        g2.setFont(FontManager.TABLE_AMOUNT);
                        g2.setColor(ThemeColors.EXPENSE);
                        g2.drawString(CurrencyFormatter.format(amt), 350, 22);

                        g2.setFont(FontManager.BODY);
                        g2.setColor(ThemeColors.TEXT_MUTED);
                        g2.drawString(String.format("%.1f%%", pct), w - 120, 22);

                        g2.setColor(ThemeColors.DIVIDER);
                        g2.drawLine(0, h - 1, w, h - 1);
                        g2.dispose();
                    }
                };
                rows.add(rowPanel);
                idx++;
            }
            breakdownTablePanel.add(rows, BorderLayout.CENTER);
        }

        breakdownTablePanel.revalidate();
        breakdownTablePanel.repaint();
    }

    private void exportReportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("financial_report_" + DateUtils.getCurrentMonthKey() + ".csv"));
        int res = chooser.showSaveDialog(parentFrame);

        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            int userId = AuthenticationService.getCurrentUser() != null ?
                    AuthenticationService.getCurrentUser().getUserId() : 1;
            new Thread(() -> {
                try {
                    boolean ok = reportService.exportUserTransactionsToCsv(userId, target);
                    SwingUtilities.invokeLater(() -> {
                        if (ok) {
                            JOptionPane.showMessageDialog(parentFrame,
                                "Detailed financial report exported successfully to " + target.getName(),
                                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(parentFrame,
                                "Failed to export report to CSV.",
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
