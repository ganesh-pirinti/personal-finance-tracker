package ui.admin;

import model.User;
import service.ApplicationTextService;
import service.AuditLogService;
import ui.components.CardPanel;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Screen: Form & Field Configuration
 * Allows administrators to toggle mandatory fields, default form categories,
 * and dashboard widget visibility without breaking database schema integrity.
 */
public class FieldConfigPanel extends JPanel {
    private final User currentAdmin;
    private final ApplicationTextService textService;
    private final AuditLogService auditLogService;

    // Controls
    private JCheckBox chkMobileRequired;
    private JCheckBox chkDobRequired;

    private JComboBox<String> cmbDefaultType;
    private JComboBox<String> cmbDefaultCategory;
    private JCheckBox chkRequireDescription;

    private JCheckBox chkShowCashFlowChart;
    private JCheckBox chkShowCategoryChart;
    private JCheckBox chkShowRecentTable;

    public FieldConfigPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.textService = ApplicationTextService.getInstance();
        this.auditLogService = new AuditLogService();

        initUI();
        loadSettings();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP HEADER & SAVE BUTTON
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Form Field Rules & Widget Controls");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSubtitle = new JLabel("Manage input constraints, default transaction attributes, and dashboard widget layouts");
        lblSubtitle.setFont(FontManager.BODY);
        lblSubtitle.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        CustomButton btnSave = new CustomButton("Save Form Rules", CustomButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> saveSettings());
        headerPanel.add(btnSave, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER CARDS GRID
        JPanel centerGrid = new JPanel(new GridLayout(3, 1, 0, 16));
        centerGrid.setOpaque(false);

        // CARD 1: Registration Form Rules
        CardPanel regCard = new CardPanel();
        regCard.setLayout(new BorderLayout(0, 10));
        regCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblRegTitle = new JLabel("1. User Registration Constraints");
        lblRegTitle.setFont(FontManager.CARD_TITLE);
        lblRegTitle.setForeground(ThemeColors.PRIMARY);
        regCard.add(lblRegTitle, BorderLayout.NORTH);

        JPanel regBody = new JPanel(new GridLayout(2, 1, 0, 8));
        regBody.setOpaque(false);
        chkMobileRequired = new JCheckBox("Strictly require valid mobile phone number during user registration");
        chkMobileRequired.setFont(FontManager.BODY);
        chkMobileRequired.setOpaque(false);

        chkDobRequired = new JCheckBox("Strictly require user date of birth during user registration");
        chkDobRequired.setFont(FontManager.BODY);
        chkDobRequired.setOpaque(false);

        regBody.add(chkMobileRequired);
        regBody.add(chkDobRequired);
        regCard.add(regBody, BorderLayout.CENTER);

        centerGrid.add(regCard);

        // CARD 2: Transaction Defaults
        CardPanel txCard = new CardPanel();
        txCard.setLayout(new BorderLayout(0, 10));
        txCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblTxTitle = new JLabel("2. Transaction Form Behavior & Defaults");
        lblTxTitle.setFont(FontManager.CARD_TITLE);
        lblTxTitle.setForeground(ThemeColors.PRIMARY);
        txCard.add(lblTxTitle, BorderLayout.NORTH);

        JPanel txBody = new JPanel(new GridBagLayout());
        txBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 6, 14);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblDefType = new JLabel("Default Transaction Type:");
        lblDefType.setFont(FontManager.BODY);
        txBody.add(lblDefType, gbc);

        gbc.gridx = 1;
        cmbDefaultType = new JComboBox<>(new String[]{"EXPENSE", "INCOME"});
        cmbDefaultType.setFont(FontManager.FORM_INPUT);
        cmbDefaultType.setPreferredSize(new Dimension(140, 30));
        txBody.add(cmbDefaultType, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblDefCat = new JLabel("Default Category Preselected:");
        lblDefCat.setFont(FontManager.BODY);
        txBody.add(lblDefCat, gbc);

        gbc.gridx = 1;
        cmbDefaultCategory = new JComboBox<>(new String[]{"Food", "Salary", "Travel", "Education", "Shopping", "Bills", "Entertainment", "Healthcare", "Freelance", "Investment"});
        cmbDefaultCategory.setFont(FontManager.FORM_INPUT);
        cmbDefaultCategory.setPreferredSize(new Dimension(140, 30));
        txBody.add(cmbDefaultCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        chkRequireDescription = new JCheckBox("Mandate detailed description text on transaction creation (reject empty descriptions)");
        chkRequireDescription.setFont(FontManager.BODY);
        chkRequireDescription.setOpaque(false);
        txBody.add(chkRequireDescription, gbc);

        txCard.add(txBody, BorderLayout.CENTER);
        centerGrid.add(txCard);

        // CARD 3: Dashboard Widgets
        CardPanel dashCard = new CardPanel();
        dashCard.setLayout(new BorderLayout(0, 10));
        dashCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblDashTitle = new JLabel("3. User Dashboard Visual Components");
        lblDashTitle.setFont(FontManager.CARD_TITLE);
        lblDashTitle.setForeground(ThemeColors.PRIMARY);
        dashCard.add(lblDashTitle, BorderLayout.NORTH);

        JPanel dashBody = new JPanel(new GridLayout(3, 1, 0, 8));
        dashBody.setOpaque(false);

        chkShowCashFlowChart = new JCheckBox("Enable Monthly Cash Flow Trends Chart (Income vs Expense)");
        chkShowCashFlowChart.setFont(FontManager.BODY);
        chkShowCashFlowChart.setOpaque(false);

        chkShowCategoryChart = new JCheckBox("Enable Category Expense Distribution Donut Chart");
        chkShowCategoryChart.setFont(FontManager.BODY);
        chkShowCategoryChart.setOpaque(false);

        chkShowRecentTable = new JCheckBox("Enable Recent Transactions Feed Table on Main Overview");
        chkShowRecentTable.setFont(FontManager.BODY);
        chkShowRecentTable.setOpaque(false);

        dashBody.add(chkShowCashFlowChart);
        dashBody.add(chkShowCategoryChart);
        dashBody.add(chkShowRecentTable);
        dashCard.add(dashBody, BorderLayout.CENTER);

        centerGrid.add(dashCard);

        JScrollPane scrollPane = new JScrollPane(centerGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSettings() {
        chkMobileRequired.setSelected(Boolean.parseBoolean(textService.getText("field.mobile_required", "false")));
        chkDobRequired.setSelected(Boolean.parseBoolean(textService.getText("field.dob_required", "false")));
        cmbDefaultType.setSelectedItem(textService.getText("field.default_tx_type", "EXPENSE"));
        cmbDefaultCategory.setSelectedItem(textService.getText("field.default_category", "Food"));
        chkRequireDescription.setSelected(Boolean.parseBoolean(textService.getText("field.require_desc", "true")));

        chkShowCashFlowChart.setSelected(Boolean.parseBoolean(textService.getText("widget.show_cashflow", "true")));
        chkShowCategoryChart.setSelected(Boolean.parseBoolean(textService.getText("widget.show_categories", "true")));
        chkShowRecentTable.setSelected(Boolean.parseBoolean(textService.getText("widget.show_recent_table", "true")));
    }

    private void saveSettings() {
        int actorId = currentAdmin != null ? currentAdmin.getUserId() : 1;
        String email = currentAdmin != null ? currentAdmin.getEmail() : "ADMIN";

        textService.setText("field.mobile_required", String.valueOf(chkMobileRequired.isSelected()), "FORMS", "Mobile required at registration", actorId);
        textService.setText("field.dob_required", String.valueOf(chkDobRequired.isSelected()), "FORMS", "Date of birth required at registration", actorId);
        textService.setText("field.default_tx_type", (String) cmbDefaultType.getSelectedItem(), "FORMS", "Default transaction type", actorId);
        textService.setText("field.default_category", (String) cmbDefaultCategory.getSelectedItem(), "FORMS", "Default transaction category", actorId);
        textService.setText("field.require_desc", String.valueOf(chkRequireDescription.isSelected()), "FORMS", "Require description flag", actorId);

        textService.setText("widget.show_cashflow", String.valueOf(chkShowCashFlowChart.isSelected()), "DASHBOARD", "Show cash flow chart", actorId);
        textService.setText("widget.show_categories", String.valueOf(chkShowCategoryChart.isSelected()), "DASHBOARD", "Show category chart", actorId);
        textService.setText("widget.show_recent_table", String.valueOf(chkShowRecentTable.isSelected()), "DASHBOARD", "Show recent table", actorId);

        auditLogService.log(actorId, email, "CONFIG_UPDATE", "FORMS", "RULES", "SUCCESS",
                "Updated application form constraints and dashboard widget configuration.");

        JOptionPane.showMessageDialog(this,
            "Form rules and widget display configuration saved successfully.",
            "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
    }
}
