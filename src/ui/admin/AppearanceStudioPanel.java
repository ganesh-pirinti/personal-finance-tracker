package ui.admin;

import model.ThemeConfiguration;
import model.User;
import service.AuditLogService;
import service.ThemeConfigurationService;
import ui.components.CardPanel;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Screen: Appearance & Branding Studio (UI/UX Customization)
 * Allows administrators to select presets, fine-tune color schemes, typography, layout density,
 * observe interactive live previews, and publish theme changes globally across the application.
 */
public class AppearanceStudioPanel extends JPanel {
    private final User currentAdmin;
    private final ThemeConfigurationService themeService;
    private final AuditLogService auditLogService;

    private ThemeConfiguration draftTheme;

    // Controls
    private JComboBox<String> cmbPresets;
    private JTextField txtPrimaryHex;
    private JTextField txtBgMainHex;
    private JTextField txtSurfaceHex;
    private JTextField txtSidebarHex;
    private JTextField txtTextMainHex;
    private JTextField txtIncomeHex;
    private JTextField txtExpenseHex;

    private JComboBox<String> cmbFontFamily;
    private JSlider sldRadius;
    private JComboBox<String> cmbDensity;

    private PreviewCanvas previewCanvas;

    public AppearanceStudioPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.themeService = ThemeConfigurationService.getInstance();
        this.auditLogService = new AuditLogService();
        this.draftTheme = themeService.getActiveTheme();

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP HEADER & GLOBAL ACTIONS
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Appearance Studio & Visual Branding");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSubtitle = new JLabel("Customize corporate palette, typography, layout density, and publish live design updates");
        lblSubtitle.setFont(FontManager.BODY);
        lblSubtitle.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnRow.setOpaque(false);

        CustomButton btnReset = new CustomButton("Revert to Defaults", CustomButton.ButtonType.SECONDARY);
        btnReset.addActionListener(e -> resetToDefaultTheme());

        CustomButton btnPublish = new CustomButton("★ Publish Theme Globally", CustomButton.ButtonType.PRIMARY);
        btnPublish.addActionListener(e -> publishTheme());

        actionBtnRow.add(btnReset);
        actionBtnRow.add(btnPublish);
        headerPanel.add(actionBtnRow, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. MAIN SPLIT: LEFT CONTROLS (FORM) / RIGHT (LIVE PREVIEW)
        JPanel contentSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        contentSplit.setOpaque(false);

        // LEFT: CONTROLS
        CardPanel controlsCard = new CardPanel();
        controlsCard.setLayout(new BorderLayout(0, 12));
        controlsCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 8, 0);

        // A. Presets Dropdown
        JLabel lblPreset = new JLabel("Theme Preset Template");
        lblPreset.setFont(FontManager.FORM_LABEL);
        formGrid.add(lblPreset, gbc);

        gbc.gridy++;
        cmbPresets = new JComboBox<>(new String[]{
            ThemeConfiguration.PRESET_LIGHT_DEFAULT,
            ThemeConfiguration.PRESET_DARK_MODERN,
            ThemeConfiguration.PRESET_SLATE_EXECUTIVE,
            ThemeConfiguration.PRESET_EMERALD_FOREST
        });
        cmbPresets.setFont(FontManager.FORM_INPUT);
        cmbPresets.setPreferredSize(new Dimension(300, 32));
        cmbPresets.addActionListener(e -> applySelectedPreset());
        formGrid.add(cmbPresets, gbc);

        // B. Color Palette Pickers
        gbc.gridy++;
        JLabel lblColorsHeader = new JLabel("Palette Hex Codes & Pickers");
        lblColorsHeader.setFont(FontManager.CARD_TITLE);
        lblColorsHeader.setForeground(ThemeColors.PRIMARY);
        formGrid.add(lblColorsHeader, gbc);

        gbc.gridy++;
        txtPrimaryHex = addColorRow(formGrid, gbc, "Primary Brand Color:", ThemeConfiguration.toHex(draftTheme.getPrimary()), c -> {
            draftTheme.setPrimary(c);
            updatePreview();
        });

        gbc.gridy++;
        txtBgMainHex = addColorRow(formGrid, gbc, "Background Canvas Color:", ThemeConfiguration.toHex(draftTheme.getBgMain()), c -> {
            draftTheme.setBgMain(c);
            updatePreview();
        });

        gbc.gridy++;
        txtSurfaceHex = addColorRow(formGrid, gbc, "Surface / Card Panel Color:", ThemeConfiguration.toHex(draftTheme.getSurface()), c -> {
            draftTheme.setSurface(c);
            updatePreview();
        });

        gbc.gridy++;
        txtSidebarHex = addColorRow(formGrid, gbc, "Sidebar Background Color:", ThemeConfiguration.toHex(draftTheme.getSidebarBg()), c -> {
            draftTheme.setSidebarBg(c);
            updatePreview();
        });

        gbc.gridy++;
        txtTextMainHex = addColorRow(formGrid, gbc, "Main Body Text Color:", ThemeConfiguration.toHex(draftTheme.getTextMain()), c -> {
            draftTheme.setTextMain(c);
            updatePreview();
        });

        gbc.gridy++;
        txtIncomeHex = addColorRow(formGrid, gbc, "Income Indicator Color:", ThemeConfiguration.toHex(draftTheme.getIncome()), c -> {
            draftTheme.setIncome(c);
            updatePreview();
        });

        gbc.gridy++;
        txtExpenseHex = addColorRow(formGrid, gbc, "Expense Indicator Color:", ThemeConfiguration.toHex(draftTheme.getExpense()), c -> {
            draftTheme.setExpense(c);
            updatePreview();
        });

        // C. Typography & Density
        gbc.gridy++;
        JLabel lblTypoHeader = new JLabel("Typography & Spatial Density");
        lblTypoHeader.setFont(FontManager.CARD_TITLE);
        lblTypoHeader.setForeground(ThemeColors.PRIMARY);
        formGrid.add(lblTypoHeader, gbc);

        gbc.gridy++;
        JLabel lblFont = new JLabel("Font Family");
        lblFont.setFont(FontManager.FORM_LABEL);
        formGrid.add(lblFont, gbc);

        gbc.gridy++;
        cmbFontFamily = new JComboBox<>(new String[]{"Segoe UI", "SansSerif", "Tahoma", "Arial", "Consolas"});
        cmbFontFamily.setFont(FontManager.FORM_INPUT);
        cmbFontFamily.setSelectedItem(draftTheme.getFontFamily());
        cmbFontFamily.addActionListener(e -> {
            draftTheme.setFontFamily((String) cmbFontFamily.getSelectedItem());
            updatePreview();
        });
        formGrid.add(cmbFontFamily, gbc);

        gbc.gridy++;
        JLabel lblCorner = new JLabel("Card Corner Radius (px)");
        lblCorner.setFont(FontManager.FORM_LABEL);
        formGrid.add(lblCorner, gbc);

        gbc.gridy++;
        sldRadius = new JSlider(8, 24, draftTheme.getCardCornerRadius());
        sldRadius.setOpaque(false);
        sldRadius.addChangeListener(e -> {
            draftTheme.setCardCornerRadius(sldRadius.getValue());
            updatePreview();
        });
        formGrid.add(sldRadius, gbc);

        gbc.gridy++;
        JLabel lblDensity = new JLabel("Layout Density Mode");
        lblDensity.setFont(FontManager.FORM_LABEL);
        formGrid.add(lblDensity, gbc);

        gbc.gridy++;
        cmbDensity = new JComboBox<>(new String[]{"COMFORTABLE", "COMPACT"});
        cmbDensity.setFont(FontManager.FORM_INPUT);
        cmbDensity.setSelectedItem(draftTheme.getLayoutDensity());
        cmbDensity.addActionListener(e -> {
            draftTheme.setLayoutDensity((String) cmbDensity.getSelectedItem());
            updatePreview();
        });
        formGrid.add(cmbDensity, gbc);

        JScrollPane scrollControls = new JScrollPane(formGrid);
        scrollControls.setBorder(null);
        scrollControls.setOpaque(false);
        scrollControls.getViewport().setOpaque(false);
        controlsCard.add(scrollControls, BorderLayout.CENTER);

        contentSplit.add(controlsCard);

        // RIGHT: LIVE PREVIEW CANVAS
        CardPanel previewContainer = new CardPanel();
        previewContainer.setLayout(new BorderLayout(0, 10));
        previewContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblPreviewTitle = new JLabel("Interactive Real-Time Preview");
        lblPreviewTitle.setFont(FontManager.CARD_TITLE);
        lblPreviewTitle.setForeground(ThemeColors.TEXT_MAIN);
        previewContainer.add(lblPreviewTitle, BorderLayout.NORTH);

        previewCanvas = new PreviewCanvas();
        previewContainer.add(previewCanvas, BorderLayout.CENTER);

        contentSplit.add(previewContainer);

        add(contentSplit, BorderLayout.CENTER);
    }

    private interface ColorConsumer {
        void accept(Color c);
    }

    private JTextField addColorRow(JPanel container, GridBagConstraints gbc, String label, String defaultHex, ColorConsumer consumer) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.BODY);
        lbl.setPreferredSize(new Dimension(190, 28));
        row.add(lbl, BorderLayout.WEST);

        JTextField txtHex = new JTextField(defaultHex);
        txtHex.setFont(FontManager.CODE);
        txtHex.setPreferredSize(new Dimension(90, 28));

        CustomButton btnPick = new CustomButton("Pick", CustomButton.ButtonType.SECONDARY);
        btnPick.setPreferredSize(new Dimension(55, 28));

        btnPick.addActionListener(e -> {
            Color initial = ThemeConfiguration.fromHex(txtHex.getText(), ThemeColors.PRIMARY);
            Color chosen = JColorChooser.showDialog(this, "Select " + label, initial);
            if (chosen != null) {
                String hex = ThemeConfiguration.toHex(chosen);
                txtHex.setText(hex);
                consumer.accept(chosen);
            }
        });

        txtHex.addActionListener(e -> {
            Color parsed = ThemeConfiguration.fromHex(txtHex.getText(), null);
            if (parsed != null) consumer.accept(parsed);
        });

        JPanel rightSub = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightSub.setOpaque(false);
        rightSub.add(txtHex);
        rightSub.add(btnPick);

        row.add(rightSub, BorderLayout.EAST);
        container.add(row, gbc);
        return txtHex;
    }

    private void applySelectedPreset() {
        String preset = (String) cmbPresets.getSelectedItem();
        if (ThemeConfiguration.PRESET_LIGHT_DEFAULT.equals(preset)) {
            draftTheme = ThemeConfiguration.createDefaultLight();
        } else if (ThemeConfiguration.PRESET_DARK_MODERN.equals(preset)) {
            draftTheme = ThemeConfiguration.createDarkModern();
        } else if (ThemeConfiguration.PRESET_SLATE_EXECUTIVE.equals(preset)) {
            draftTheme = ThemeConfiguration.createSlateExecutive();
        } else if (ThemeConfiguration.PRESET_EMERALD_FOREST.equals(preset)) {
            draftTheme = ThemeConfiguration.createEmeraldForest();
        }

        // Update Text Fields
        txtPrimaryHex.setText(ThemeConfiguration.toHex(draftTheme.getPrimary()));
        txtBgMainHex.setText(ThemeConfiguration.toHex(draftTheme.getBgMain()));
        txtSurfaceHex.setText(ThemeConfiguration.toHex(draftTheme.getSurface()));
        txtSidebarHex.setText(ThemeConfiguration.toHex(draftTheme.getSidebarBg()));
        txtTextMainHex.setText(ThemeConfiguration.toHex(draftTheme.getTextMain()));
        txtIncomeHex.setText(ThemeConfiguration.toHex(draftTheme.getIncome()));
        txtExpenseHex.setText(ThemeConfiguration.toHex(draftTheme.getExpense()));
        cmbFontFamily.setSelectedItem(draftTheme.getFontFamily());
        sldRadius.setValue(draftTheme.getCardCornerRadius());
        cmbDensity.setSelectedItem(draftTheme.getLayoutDensity());

        updatePreview();
    }

    private void updatePreview() {
        if (previewCanvas != null) {
            previewCanvas.repaint();
        }
    }

    private void publishTheme() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Publishing will update the application styling across all active screens and store this configuration in MySQL.\nProceed?",
            "Confirm Global Theme Publication", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            themeService.publishTheme(draftTheme, currentAdmin != null ? currentAdmin.getUserId() : 1);
            auditLogService.log(currentAdmin != null ? currentAdmin.getUserId() : 1,
                    currentAdmin != null ? currentAdmin.getEmail() : "ADMIN",
                    "THEME_PUBLISH", "THEME", draftTheme.getName(), "SUCCESS",
                    "Published theme globally: " + draftTheme.getName() + " (Font: " + draftTheme.getFontFamily() + ")");

            JOptionPane.showMessageDialog(this,
                "Theme '" + draftTheme.getName() + "' published globally! The user interface has been updated.",
                "Theme Published", JOptionPane.INFORMATION_MESSAGE);

            // Trigger repaint on entire root frame
            if (getRootPane() != null) {
                getRootPane().repaint();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error publishing theme: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetToDefaultTheme() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Revert all custom styling to system default light theme?",
            "Revert Styling", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        themeService.resetToDefault(currentAdmin != null ? currentAdmin.getUserId() : 1);
        draftTheme = themeService.getActiveTheme();
        cmbPresets.setSelectedItem(ThemeConfiguration.PRESET_LIGHT_DEFAULT);
        applySelectedPreset();
        JOptionPane.showMessageDialog(this, "Theme restored to factory defaults.", "Theme Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Custom Graphics2D Live Mock Preview Component
     */
    private class PreviewCanvas extends JPanel {
        public PreviewCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(380, 480));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Mock Application Background
            g2.setColor(draftTheme.getBgMain());
            g2.fillRect(0, 0, w, h);

            // 2. Mock Mini Sidebar
            int sbW = 90;
            g2.setColor(draftTheme.getSidebarBg());
            g2.fillRect(0, 0, sbW, h);
            g2.setColor(draftTheme.getBorder());
            g2.drawLine(sbW, 0, sbW, h);

            // Mini Nav Items
            g2.setColor(draftTheme.getPrimary());
            g2.fillRoundRect(8, 20, sbW - 16, 22, 6, 6);
            g2.setColor(Color.WHITE);
            Font mockFont = new Font(draftTheme.getFontFamily(), Font.BOLD, 10);
            g2.setFont(mockFont);
            g2.drawString("Dashboard", 14, 35);

            g2.setColor(draftTheme.getTextMuted());
            Font plainFont = new Font(draftTheme.getFontFamily(), Font.PLAIN, 10);
            g2.setFont(plainFont);
            g2.drawString("Transactions", 14, 62);
            g2.drawString("Budgets", 14, 86);
            g2.drawString("Reports", 14, 110);

            // 3. Mock Top Bar
            int topH = 36;
            g2.setColor(draftTheme.getHeaderBg());
            g2.fillRect(sbW + 1, 0, w - sbW, topH);
            g2.setColor(draftTheme.getBorder());
            g2.drawLine(sbW + 1, topH, w, topH);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.BOLD, 12));
            g2.setColor(draftTheme.getTextMain());
            g2.drawString("Personal Wealth Suite", sbW + 14, 23);

            // 4. Mock Card 1: Balance Card
            int cardX = sbW + 14;
            int cardY = topH + 16;
            int cardW = w - sbW - 28;
            int cardH = 90;
            int radius = draftTheme.getCardCornerRadius();

            g2.setColor(draftTheme.getSurface());
            g2.fillRoundRect(cardX, cardY, cardW, cardH, radius, radius);
            g2.setColor(draftTheme.getBorder());
            g2.drawRoundRect(cardX, cardY, cardW - 1, cardH - 1, radius, radius);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.PLAIN, 11));
            g2.setColor(draftTheme.getTextMuted());
            g2.drawString("Total Available Balance", cardX + 14, cardY + 24);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.BOLD, 20));
            g2.setColor(draftTheme.getBalance());
            g2.drawString("₹ 62,500.00", cardX + 14, cardY + 52);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.PLAIN, 10));
            g2.setColor(draftTheme.getIncome());
            g2.drawString("▲ +12.4% vs last month", cardX + 14, cardY + 74);

            // 5. Mock Mini Financial Stats Grid
            int statY = cardY + cardH + 14;
            int halfW = (cardW - 10) / 2;
            int statH = 75;

            // Income Box
            g2.setColor(draftTheme.getSurface());
            g2.fillRoundRect(cardX, statY, halfW, statH, radius, radius);
            g2.setColor(draftTheme.getBorder());
            g2.drawRoundRect(cardX, statY, halfW - 1, statH - 1, radius, radius);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.PLAIN, 10));
            g2.setColor(draftTheme.getTextMuted());
            g2.drawString("Monthly Inflow", cardX + 10, statY + 22);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.BOLD, 15));
            g2.setColor(draftTheme.getIncome());
            g2.drawString("₹ 82,500.00", cardX + 10, statY + 46);

            // Expense Box
            int expX = cardX + halfW + 10;
            g2.setColor(draftTheme.getSurface());
            g2.fillRoundRect(expX, statY, halfW, statH, radius, radius);
            g2.setColor(draftTheme.getBorder());
            g2.drawRoundRect(expX, statY, halfW - 1, statH - 1, radius, radius);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.PLAIN, 10));
            g2.setColor(draftTheme.getTextMuted());
            g2.drawString("Monthly Outflow", expX + 10, statY + 22);

            g2.setFont(new Font(draftTheme.getFontFamily(), Font.BOLD, 15));
            g2.setColor(draftTheme.getExpense());
            g2.drawString("₹ 36,100.00", expX + 10, statY + 46);

            // 6. Mock Interactive Action Button
            int btnY = statY + statH + 16;
            g2.setColor(draftTheme.getPrimary());
            g2.fillRoundRect(cardX, btnY, cardW, 32, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(draftTheme.getFontFamily(), Font.BOLD, 11));
            g2.drawString("+ Add New Transaction Record", cardX + (cardW / 2) - 80, btnY + 20);

            // 7. Density Indicator
            g2.setColor(draftTheme.getTextMuted());
            g2.setFont(new Font(draftTheme.getFontFamily(), Font.PLAIN, 9));
            g2.drawString("Theme: " + draftTheme.getName() + " | Density: " + draftTheme.getLayoutDensity() + " | Radius: " + radius + "px", cardX, btnY + 54);

            g2.dispose();
        }
    }
}
