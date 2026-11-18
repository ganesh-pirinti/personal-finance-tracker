package ui.admin;

import model.SystemMetrics;
import model.User;
import service.AdminAnalyticsService;
import ui.components.CardPanel;
import ui.components.CustomButton;
import ui.components.DonutChartCanvas;
import util.CurrencyFormatter;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Screen: Platform Financial Analytics
 * Visualizes aggregate cash flows, global category spending distributions,
 * and high-level platform economic indicators with strict data privacy guarantees.
 */
public class FinancialAnalyticsPanel extends JPanel {
    private final User currentAdmin;
    private final AdminAnalyticsService analyticsService;

    private JLabel lblTotalVolume;
    private JLabel lblNetFlow;
    private JLabel lblAvgTx;
    private JLabel lblTopCategory;

    private PlatformCashFlowCanvas flowChart;
    private DonutChartCanvas categoryDonutChart;
    private SystemMetrics cachedMetrics;

    public FinancialAnalyticsPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.analyticsService = new AdminAnalyticsService();

        initUI();
        refreshAnalytics();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP HEADER & PRIVACY NOTICE
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("System-Wide Financial Analytics");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSubtitle = new JLabel("Aggregated cash flow distribution & category analytics • End-to-end user privacy protected");
        lblSubtitle.setFont(FontManager.BODY);
        lblSubtitle.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        CustomButton btnRefresh = new CustomButton("⟳ Refresh Analytics", CustomButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshAnalytics());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER CONTENT SCROLLABLE
        JPanel centerGrid = new JPanel(new BorderLayout(0, 16));
        centerGrid.setOpaque(false);

        // A. 4 METRIC TILES
        JPanel tilesRow = new JPanel(new GridLayout(1, 4, 14, 0));
        tilesRow.setOpaque(false);

        tilesRow.add(createStatCard("Platform Cumulative Volume", lblTotalVolume = new JLabel("₹ 0.00"), ThemeColors.PRIMARY));
        tilesRow.add(createStatCard("Net Platform Cash Flow", lblNetFlow = new JLabel("₹ 0.00"), ThemeColors.BALANCE));
        tilesRow.add(createStatCard("Avg Transaction Size", lblAvgTx = new JLabel("₹ 0.00"), ThemeColors.TEXT_MAIN));
        tilesRow.add(createStatCard("Top Spending Category", lblTopCategory = new JLabel("None"), ThemeColors.EXPENSE));

        centerGrid.add(tilesRow, BorderLayout.NORTH);

        // B. TWO CHARTS SIDE-BY-SIDE
        JPanel chartsSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsSplit.setOpaque(false);

        // Chart 1: Platform Inflow vs Outflow
        CardPanel flowCard = new CardPanel();
        flowCard.setLayout(new BorderLayout(0, 10));
        flowCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        flowCard.setPreferredSize(new Dimension(450, 340));

        JLabel lblFlowTitle = new JLabel("Platform Total Inflow vs Total Outflow");
        lblFlowTitle.setFont(FontManager.CARD_TITLE);
        lblFlowTitle.setForeground(ThemeColors.TEXT_MAIN);
        flowCard.add(lblFlowTitle, BorderLayout.NORTH);

        flowChart = new PlatformCashFlowCanvas();
        flowCard.add(flowChart, BorderLayout.CENTER);
        chartsSplit.add(flowCard);

        // Chart 2: Category Breakdown
        CardPanel categoryCard = new CardPanel();
        categoryCard.setLayout(new BorderLayout(0, 10));
        categoryCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        categoryCard.setPreferredSize(new Dimension(450, 340));

        JLabel lblCatTitle = new JLabel("Global Spending Distribution by Category");
        lblCatTitle.setFont(FontManager.CARD_TITLE);
        lblCatTitle.setForeground(ThemeColors.TEXT_MAIN);
        categoryCard.add(lblCatTitle, BorderLayout.NORTH);

        categoryDonutChart = new DonutChartCanvas();
        categoryCard.add(categoryDonutChart, BorderLayout.CENTER);
        chartsSplit.add(categoryCard);

        centerGrid.add(chartsSplit, BorderLayout.CENTER);

        // C. BOTTOM PRIVACY BANNER
        JPanel privacyBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        privacyBanner.setOpaque(false);
        JLabel lblPrivacy = new JLabel("🔒 Enterprise Privacy Guard: Aggregated figures only. User transaction descriptions & individual balances are obscured.");
        lblPrivacy.setFont(FontManager.SMALL_BUTTON);
        lblPrivacy.setForeground(ThemeColors.PRIMARY);
        privacyBanner.add(lblPrivacy);

        centerGrid.add(privacyBanner, BorderLayout.SOUTH);

        JScrollPane mainScroll = new JScrollPane(centerGrid);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        add(mainScroll, BorderLayout.CENTER);
    }

    private CardPanel createStatCard(String title, JLabel valueLabel, Color valColor) {
        CardPanel card = new CardPanel();
        card.setLayout(new GridLayout(2, 1, 0, 4));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FontManager.SMALL);
        lbl.setForeground(ThemeColors.TEXT_MUTED);

        valueLabel.setFont(FontManager.METRIC_AMOUNT);
        valueLabel.setForeground(valColor);

        card.add(lbl);
        card.add(valueLabel);
        return card;
    }

    public void refreshAnalytics() {
        try {
            cachedMetrics = analyticsService.getSystemMetrics(currentAdmin);

            BigDecimal inc = cachedMetrics.getAggregateIncome();
            BigDecimal exp = cachedMetrics.getAggregateExpense();
            BigDecimal vol = inc.add(exp);

            lblTotalVolume.setText(CurrencyFormatter.format(vol.doubleValue()));
            lblNetFlow.setText(CurrencyFormatter.format(cachedMetrics.getAggregateNetCashFlow().doubleValue()));

            int totalTx = cachedMetrics.getTotalTransactions();
            double avg = totalTx > 0 ? (vol.doubleValue() / totalTx) : 0.0;
            lblAvgTx.setText(CurrencyFormatter.format(avg));

            Map<String, BigDecimal> catMap = cachedMetrics.getCategoryExpenses();
            String topCat = "None";
            BigDecimal maxExp = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : catMap.entrySet()) {
                if (entry.getValue().compareTo(maxExp) > 0) {
                    maxExp = entry.getValue();
                    topCat = entry.getKey() + " (" + CurrencyFormatter.format(entry.getValue().doubleValue()) + ")";
                }
            }
            lblTopCategory.setText(topCat);

            // Update Charts
            flowChart.updateData(inc.doubleValue(), exp.doubleValue());

            // Convert category expenses to double map
            Map<String, Double> dblCat = new java.util.HashMap<>();
            for (Map.Entry<String, BigDecimal> e : catMap.entrySet()) {
                dblCat.put(e.getKey(), e.getValue().doubleValue());
            }
            categoryDonutChart.setData(dblCat);

        } catch (Exception e) {
            System.err.println("Failed to refresh analytics: " + e.getMessage());
        }
    }

    private class PlatformCashFlowCanvas extends JPanel {
        private double income = 0;
        private double expense = 0;

        public PlatformCashFlowCanvas() {
            setOpaque(false);
        }

        public void updateData(double income, double expense) {
            this.income = income;
            this.expense = expense;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            double max = Math.max(income, expense);
            if (max <= 0) max = 1.0;

            int barWidth = Math.min(80, (w - 120) / 2);
            int maxHeight = h - 90;

            int incHeight = (int) ((income / max) * maxHeight);
            int expHeight = (int) ((expense / max) * maxHeight);

            int incX = (w / 2) - barWidth - 25;
            int expX = (w / 2) + 25;
            int baseY = h - 45;

            // Inflow Bar
            g2.setColor(ThemeColors.INCOME);
            g2.fillRoundRect(incX, baseY - incHeight, barWidth, incHeight, 10, 10);

            // Outflow Bar
            g2.setColor(ThemeColors.EXPENSE);
            g2.fillRoundRect(expX, baseY - expHeight, barWidth, expHeight, 10, 10);

            // Base line
            g2.setColor(ThemeColors.BORDER);
            g2.drawLine(40, baseY, w - 40, baseY);

            // Labels
            g2.setFont(FontManager.SMALL_BUTTON);
            g2.setColor(ThemeColors.TEXT_MAIN);
            g2.drawString("Total Inflow", incX + 8, baseY + 20);
            g2.drawString("Total Outflow", expX + 8, baseY + 20);

            // Amounts on top of bars
            g2.setFont(FontManager.BODY_BOLD);
            g2.setColor(ThemeColors.INCOME);
            g2.drawString(CurrencyFormatter.format(income), incX - 10, Math.max(20, baseY - incHeight - 8));

            g2.setColor(ThemeColors.EXPENSE);
            g2.drawString(CurrencyFormatter.format(expense), expX - 10, Math.max(20, baseY - expHeight - 8));

            g2.dispose();
        }
    }
}
