package ui.admin;

import model.ApplicationSetting;
import model.User;
import service.ApplicationTextService;
import service.AuditLogService;
import ui.components.CardPanel;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen: Content and Text Editor
 * Allows administrators to customize application titles, subtitles, greetings, button labels,
 * and form instructions without code modification.
 */
public class ContentEditorPanel extends JPanel {
    private final User currentAdmin;
    private final ApplicationTextService textService;
    private final AuditLogService auditLogService;

    private JTextField txtSearch;
    private JComboBox<String> cmbCategory;
    private JTable settingsTable;
    private DefaultTableModel tableModel;

    private JTextField txtEditKey;
    private JTextArea txtEditValue;
    private JLabel lblEditDesc;
    private CustomButton btnSaveText;

    private List<ApplicationSetting> currentSettings = new ArrayList<>();

    public ContentEditorPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.textService = ApplicationTextService.getInstance();
        this.auditLogService = new AuditLogService();

        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP HEADER & RESET
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Application Text & Content Management");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSubtitle = new JLabel("Configure dynamic titles, greetings, button labels, and system messages with instant live sync");
        lblSubtitle.setFont(FontManager.BODY);
        lblSubtitle.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        CustomButton btnResetAll = new CustomButton("Revert to Default Strings", CustomButton.ButtonType.SECONDARY);
        btnResetAll.addActionListener(e -> resetAllToDefaults());
        headerPanel.add(btnResetAll, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. SEARCH & CATEGORY FILTER ROW
        CardPanel filterCard = new CardPanel();
        filterCard.setLayout(new GridBagLayout());
        filterCard.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.insets = new Insets(0, 4, 0, 8);
        fgbc.fill = GridBagConstraints.HORIZONTAL;

        fgbc.gridx = 0;
        fgbc.weightx = 0.6;
        txtSearch = new JTextField();
        txtSearch.setFont(FontManager.FORM_INPUT);
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search setting key or string content...");
        txtSearch.addActionListener(e -> applyFilter());
        filterCard.add(txtSearch, fgbc);

        fgbc.gridx = 1;
        fgbc.weightx = 0.3;
        cmbCategory = new JComboBox<>(new String[]{"All Categories", "BRANDING", "AUTH", "DASHBOARD", "FORMS", "ALERTS"});
        cmbCategory.setFont(FontManager.FORM_INPUT);
        cmbCategory.setPreferredSize(new Dimension(140, 32));
        cmbCategory.addActionListener(e -> applyFilter());
        filterCard.add(cmbCategory, fgbc);

        fgbc.gridx = 2;
        fgbc.weightx = 0.1;
        CustomButton btnFilter = new CustomButton("Filter", CustomButton.ButtonType.PRIMARY);
        btnFilter.setPreferredSize(new Dimension(80, 32));
        btnFilter.addActionListener(e -> applyFilter());
        filterCard.add(btnFilter, fgbc);

        // 3. CENTER: TABLE (TOP) + INLINE EDITOR (BOTTOM)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(filterCard, BorderLayout.NORTH);

        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] cols = {"Key Identifier", "Configured Text String", "Category", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        settingsTable = new JTable(tableModel);
        settingsTable.setRowHeight(30);
        settingsTable.setFont(FontManager.TABLE_CELL);
        settingsTable.getTableHeader().setFont(FontManager.TABLE_HEADER);
        settingsTable.getTableHeader().setBackground(ThemeColors.SURFACE_HOVER);
        settingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        settingsTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        settingsTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        settingsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        settingsTable.getColumnModel().getColumn(3).setPreferredWidth(250);

        settingsTable.getSelectionModel().addListSelectionListener(e -> loadSelectedSettingIntoEditor());

        JScrollPane scrollTable = new JScrollPane(settingsTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);

        // BOTTOM: INLINE EDITOR CARD
        CardPanel editorCard = new CardPanel();
        editorCard.setLayout(new BorderLayout(0, 10));
        editorCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        editorCard.setPreferredSize(new Dimension(800, 170));

        JPanel editorTopRow = new JPanel(new BorderLayout());
        editorTopRow.setOpaque(false);

        txtEditKey = new JTextField("Select a string key above to edit...");
        txtEditKey.setFont(FontManager.BODY_BOLD);
        txtEditKey.setEditable(false);
        txtEditKey.setBorder(null);
        txtEditKey.setOpaque(false);
        editorTopRow.add(txtEditKey, BorderLayout.WEST);

        lblEditDesc = new JLabel("");
        lblEditDesc.setFont(FontManager.SMALL);
        lblEditDesc.setForeground(ThemeColors.TEXT_MUTED);
        editorTopRow.add(lblEditDesc, BorderLayout.EAST);

        editorCard.add(editorTopRow, BorderLayout.NORTH);

        txtEditValue = new JTextArea();
        txtEditValue.setFont(FontManager.FORM_INPUT);
        txtEditValue.setLineWrap(true);
        txtEditValue.setWrapStyleWord(true);
        JScrollPane scrollEdit = new JScrollPane(txtEditValue);
        scrollEdit.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        editorCard.add(scrollEdit, BorderLayout.CENTER);

        JPanel editorBottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        editorBottomRow.setOpaque(false);

        btnSaveText = new CustomButton("Save String Update", CustomButton.ButtonType.PRIMARY);
        btnSaveText.setEnabled(false);
        btnSaveText.addActionListener(e -> saveSelectedText());
        editorBottomRow.add(btnSaveText);

        editorCard.add(editorBottomRow, BorderLayout.SOUTH);

        centerPanel.add(editorCard, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void refreshTable() {
        textService.refreshCache();
        applyFilter();
    }

    private void applyFilter() {
        String query = txtSearch.getText().trim().toLowerCase();
        String cat = (String) cmbCategory.getSelectedItem();
        boolean filterCat = cat != null && !"All Categories".equals(cat);

        List<ApplicationSetting> all = textService.getAllSettings();
        currentSettings.clear();
        tableModel.setRowCount(0);

        for (ApplicationSetting s : all) {
            boolean matchQuery = query.isEmpty() ||
                    s.getSettingKey().toLowerCase().contains(query) ||
                    s.getSettingValue().toLowerCase().contains(query);

            boolean matchCat = !filterCat || (s.getCategory() != null && s.getCategory().equalsIgnoreCase(cat));

            if (matchQuery && matchCat) {
                currentSettings.add(s);
                tableModel.addRow(new Object[]{
                    s.getSettingKey(),
                    s.getSettingValue(),
                    s.getCategory(),
                    s.getDescription() != null ? s.getDescription() : ""
                });
            }
        }

        btnSaveText.setEnabled(false);
        txtEditKey.setText("Select a string key above to edit...");
        txtEditValue.setText("");
        lblEditDesc.setText("");
    }

    private void loadSelectedSettingIntoEditor() {
        int row = settingsTable.getSelectedRow();
        if (row < 0 || row >= currentSettings.size()) {
            btnSaveText.setEnabled(false);
            return;
        }

        ApplicationSetting s = currentSettings.get(row);
        txtEditKey.setText(s.getSettingKey() + "  [" + s.getCategory() + "]");
        txtEditValue.setText(s.getSettingValue());
        lblEditDesc.setText(s.getDescription() != null ? s.getDescription() : "");
        btnSaveText.setEnabled(true);
    }

    private void saveSelectedText() {
        int row = settingsTable.getSelectedRow();
        if (row < 0 || row >= currentSettings.size()) return;

        ApplicationSetting s = currentSettings.get(row);
        String newValue = txtEditValue.getText().trim();
        if (newValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Text value cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        textService.setText(s.getSettingKey(), newValue, s.getCategory(), s.getDescription(),
                currentAdmin != null ? currentAdmin.getUserId() : 1);

        auditLogService.log(currentAdmin != null ? currentAdmin.getUserId() : 1,
                currentAdmin != null ? currentAdmin.getEmail() : "ADMIN",
                "TEXT_UPDATE", "CONFIG", s.getSettingKey(), "SUCCESS",
                "Updated UI text for key: " + s.getSettingKey() + " -> \"" + newValue + "\"");

        JOptionPane.showMessageDialog(this,
            "Text updated successfully for '" + s.getSettingKey() + "'.",
            "String Saved", JOptionPane.INFORMATION_MESSAGE);

        refreshTable();
    }

    private void resetAllToDefaults() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Reset all dynamic titles, subtitles, and labels back to factory defaults?",
            "Confirm Reset", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        textService.resetToDefaults(currentAdmin != null ? currentAdmin.getUserId() : 1);
        auditLogService.log(currentAdmin != null ? currentAdmin.getUserId() : 1,
                currentAdmin != null ? currentAdmin.getEmail() : "ADMIN",
                "TEXT_RESET", "CONFIG", "ALL", "SUCCESS",
                "Reverted all UI text settings to factory defaults.");

        JOptionPane.showMessageDialog(this, "All UI text settings restored to defaults.", "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
        refreshTable();
    }
}
