package ui.components;

import model.Transaction;
import util.CurrencyFormatter;
import util.DateUtils;
import util.FontManager;
import util.ThemeColors;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Swing JTable component for displaying transactions with custom renderers,
 * alternating row colors, type badges, signed amounts, and action buttons.
 */
public class TransactionTablePanel extends JPanel {
    public interface TransactionActionListener {
        void onEdit(Transaction tx);
        void onDelete(Transaction tx);
    }

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JScrollPane scrollPane;
    private final JPanel emptyPanel;
    private TransactionActionListener listener;
    private List<Transaction> currentTransactions = new ArrayList<>();

    private static final String[] COLUMN_NAMES = {
        "TYPE", "DATE", "CATEGORY", "DESCRIPTION", "AMOUNT", "ACTIONS"
    };

    public TransactionTablePanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColors.SURFACE);

        // Define Table Model with non-editable data cells, editable actions column
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column is interactive
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(FontManager.TABLE_CELL);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(ThemeColors.TEXT_MAIN);

        // Header Styling
        table.getTableHeader().setFont(FontManager.TABLE_HEADER);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(ThemeColors.TEXT_MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(700, 36));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER));

        // Column Widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Type
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Category
        table.getColumnModel().getColumn(3).setPreferredWidth(210); // Description
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Amount
        table.getColumnModel().getColumn(5).setPreferredWidth(130); // Actions

        // Custom Cell Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new TypeBadgeRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new DateCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultPaddedRenderer(FontManager.BODY_BOLD, ThemeColors.TEXT_MAIN));
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultPaddedRenderer(FontManager.TABLE_CELL, ThemeColors.TEXT_MUTED));
        table.getColumnModel().getColumn(4).setCellRenderer(new AmountCellRenderer());

        ActionCellRendererEditor actionRendererEditor = new ActionCellRendererEditor();
        table.getColumnModel().getColumn(5).setCellRenderer(actionRendererEditor);
        table.getColumnModel().getColumn(5).setCellEditor(actionRendererEditor);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.SURFACE);

        // Empty state panel
        emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(ThemeColors.SURFACE);
        emptyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        updateEmptyPanel();

        add(scrollPane, BorderLayout.CENTER);
    }

    public interface EmptyActionCallback {
        void onAddIncome();
        void onAddExpense();
    }
    private EmptyActionCallback emptyCallback;

    public void setEmptyActionCallback(EmptyActionCallback callback) {
        this.emptyCallback = callback;
        updateEmptyPanel();
    }

    private void updateEmptyPanel() {
        emptyPanel.removeAll();
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new javax.swing.BoxLayout(centerBox, javax.swing.BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JLabel lblTitle = new JLabel("No Transactions Recorded Yet", SwingConstants.CENTER);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setFont(FontManager.BODY_BOLD);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSub = new JLabel("Start tracking by recording your first income or expense for this period.", SwingConstants.CENTER);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setFont(FontManager.BODY);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        centerBox.add(javax.swing.Box.createVerticalGlue());
        centerBox.add(lblTitle);
        centerBox.add(javax.swing.Box.createVerticalStrut(6));
        centerBox.add(lblSub);

        if (emptyCallback != null) {
            centerBox.add(javax.swing.Box.createVerticalStrut(14));
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            btnRow.setOpaque(false);

            CustomButton btnAddIncome = new CustomButton("+ Add Income", CustomButton.Style.PRIMARY);
            btnAddIncome.setCustomFont(FontManager.SMALL_BUTTON);
            btnAddIncome.setPreferredSize(new Dimension(130, 32));
            btnAddIncome.addActionListener(e -> emptyCallback.onAddIncome());

            CustomButton btnAddExpense = new CustomButton("+ Add Expense", CustomButton.Style.SECONDARY);
            btnAddExpense.setCustomFont(FontManager.SMALL_BUTTON);
            btnAddExpense.setPreferredSize(new Dimension(130, 32));
            btnAddExpense.addActionListener(e -> emptyCallback.onAddExpense());

            btnRow.add(btnAddIncome);
            btnRow.add(btnAddExpense);
            centerBox.add(btnRow);
        }
        centerBox.add(javax.swing.Box.createVerticalGlue());

        emptyPanel.add(centerBox, BorderLayout.CENTER);
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }

    public void setListener(TransactionActionListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.currentTransactions = transactions != null ? transactions : new ArrayList<>();
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        if (currentTransactions.isEmpty()) {
            remove(scrollPane);
            add(emptyPanel, BorderLayout.CENTER);
        } else {
            remove(emptyPanel);
            add(scrollPane, BorderLayout.CENTER);

            for (Transaction tx : currentTransactions) {
                tableModel.addRow(new Object[]{
                    tx.getTransactionType(),
                    DateUtils.formatDisplay(tx.getTransactionDate()),
                    tx.getCategory(),
                    tx.getDescription(),
                    tx,
                    tx
                });
            }
        }

        revalidate();
        repaint();
    }

    // -------------------------------------------------------------
    // RENDERERS & EDITORS
    // -------------------------------------------------------------

    private static class DefaultPaddedRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color color;

        public DefaultPaddedRenderer(Font font, Color color) {
            this.font = font;
            this.color = color;
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(font);
            setForeground(color);
            setBackground(row % 2 == 1 ? new Color(248, 250, 252) : ThemeColors.SURFACE);
            return this;
        }
    }

    private static class DateCellRenderer extends DefaultTableCellRenderer {
        public DateCellRenderer() {
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            setFont(FontManager.TABLE_CELL);
            setForeground(ThemeColors.TEXT_MUTED);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setBackground(row % 2 == 1 ? new Color(248, 250, 252) : ThemeColors.SURFACE);
            return this;
        }
    }

    private static class TypeBadgeRenderer extends JPanel implements TableCellRenderer {
        private String type = "INCOME";

        public TypeBadgeRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            this.type = value != null ? value.toString() : "INCOME";
            setBackground(row % 2 == 1 ? new Color(248, 250, 252) : ThemeColors.SURFACE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean isInc = "INCOME".equalsIgnoreCase(type);
            Color bg = isInc ? ThemeColors.INCOME_LIGHT : ThemeColors.EXPENSE_LIGHT;
            Color fg = isInc ? ThemeColors.INCOME : ThemeColors.EXPENSE;

            int badgeW = 76;
            int badgeH = 22;
            int x = 16;
            int y = (getHeight() - badgeH) / 2;

            g2.setColor(bg);
            g2.fillRoundRect(x, y, badgeW, badgeH, 10, 10);

            g2.setFont(FontManager.SMALL_BUTTON);
            g2.setColor(fg);
            String txt = isInc ? "INCOME" : "EXPENSE";
            int tx = x + (badgeW - g2.getFontMetrics().stringWidth(txt)) / 2;
            int ty = y + ((badgeH - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent() - 1;
            g2.drawString(txt, tx, ty);

            g2.dispose();
        }
    }

    private static class AmountCellRenderer extends DefaultTableCellRenderer {
        public AmountCellRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value instanceof Transaction) {
                Transaction tx = (Transaction) value;
                boolean isInc = tx.isIncome();
                setText(CurrencyFormatter.formatSigned(tx.getAmount().doubleValue(), isInc));
                setForeground(isInc ? ThemeColors.INCOME : ThemeColors.EXPENSE);
                setFont(FontManager.TABLE_AMOUNT);
            }
            setBackground(row % 2 == 1 ? new Color(248, 250, 252) : ThemeColors.SURFACE);
            return this;
        }
    }

    private class ActionCellRendererEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final CustomButton btnEdit;
        private final CustomButton btnDelete;
        private Transaction currentTx;

        public ActionCellRendererEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            panel.setOpaque(true);

            btnEdit = new CustomButton("Edit", CustomButton.Style.OUTLINE);
            btnEdit.setCustomFont(FontManager.SMALL_BUTTON);
            btnEdit.setPreferredSize(new Dimension(52, 26));
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (listener != null && currentTx != null) {
                    listener.onEdit(currentTx);
                }
            });

            btnDelete = new CustomButton("Del", CustomButton.Style.DANGER);
            btnDelete.setCustomFont(FontManager.SMALL_BUTTON);
            btnDelete.setPreferredSize(new Dimension(48, 26));
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (listener != null && currentTx != null) {
                    listener.onDelete(currentTx);
                }
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            panel.setBackground(row % 2 == 1 ? new Color(248, 250, 252) : ThemeColors.SURFACE);
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            this.currentTx = (Transaction) value;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentTx;
        }
    }
}
