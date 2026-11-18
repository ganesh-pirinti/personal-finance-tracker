package ui.admin;

import model.User;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;

/**
 * Super Admin Panel: Content & Field Configuration Editor.
 * Combines dynamic UI branding/labels customization and form validation rules in a cohesive tabbed console.
 */
public class ContentFieldEditorPanel extends JPanel {
    private final User currentAdmin;
    private final ContentEditorPanel contentPanel;
    private final FieldConfigPanel fieldPanel;

    public ContentFieldEditorPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.contentPanel = new ContentEditorPanel(currentAdmin);
        this.fieldPanel = new FieldConfigPanel(currentAdmin);

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FontManager.BODY_BOLD);
        tabbedPane.setBackground(ThemeColors.SURFACE);

        tabbedPane.addTab("📝 UI Content & Branding Strings", contentPanel);
        tabbedPane.addTab("⚙️ Form Field Rules & Constraints", fieldPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
}
