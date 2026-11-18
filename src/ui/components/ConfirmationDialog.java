package ui.components;

import util.FontManager;
import util.ThemeColors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Swing modal JDialog for confirming sensitive or destructive actions (Delete, Suspend, Role Change, Logout).
 */
public class ConfirmationDialog extends JDialog {
    private boolean confirmed = false;

    public ConfirmationDialog(Frame parent, String title, String message, String confirmButtonText, boolean isDanger) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        setResizable(false);
        setBackground(ThemeColors.BG_MAIN);

        // Header and Message Panel
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SURFACE);
                g2.fillRect(0, 0, w, h);

                // Title
                g2.setFont(FontManager.CARD_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString(title, 24, 34);

                // Message Text
                g2.setFont(FontManager.BODY);
                g2.setColor(ThemeColors.TEXT_MUTED);

                // Simple word wrapping for description
                String[] words = message.split(" ");
                StringBuilder line = new StringBuilder();
                int lineY = 60;
                for (String word : words) {
                    if (g2.getFontMetrics().stringWidth(line + word) > 340) {
                        g2.drawString(line.toString(), 24, lineY);
                        line = new StringBuilder(word + " ");
                        lineY += 20;
                    } else {
                        line.append(word).append(" ");
                    }
                }
                if (line.length() > 0) {
                    g2.drawString(line.toString(), 24, lineY);
                }

                g2.dispose();
            }
        };
        contentPanel.setPreferredSize(new Dimension(400, 110));
        add(contentPanel, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(ThemeColors.SURFACE);

        CustomButton btnCancel = new CustomButton("Cancel", CustomButton.Style.SECONDARY);
        btnCancel.setCustomFont(FontManager.SMALL_BUTTON);
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        CustomButton btnConfirm = new CustomButton(
            confirmButtonText,
            isDanger ? CustomButton.Style.DANGER : CustomButton.Style.PRIMARY
        );
        btnConfirm.setCustomFont(FontManager.SMALL_BUTTON);
        btnConfirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
            }
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static boolean showConfirm(Frame parent, String title, String message, String confirmButtonText, boolean isDanger) {
        ConfirmationDialog dlg = new ConfirmationDialog(parent, title, message, confirmButtonText, isDanger);
        dlg.setVisible(true);
        return dlg.isConfirmed();
    }

    public static boolean show(Component parent, String title, String message) {
        Frame frame = null;
        if (parent instanceof Frame) {
            frame = (Frame) parent;
        } else if (parent != null) {
            frame = (Frame) SwingUtilities.getWindowAncestor(parent);
        }
        return showConfirm(frame, title, message, "Confirm", false);
    }
}
