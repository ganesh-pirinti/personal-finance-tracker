package ui.admin;

import model.User;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Super Admin Panel: Interactive JDBC Architecture Visualizer.
 * Provides a high-fidelity Graphics2D interactive pipeline diagram with selectable nodes,
 * project class mappings, live node health, and step-by-step Read Flow and Write Flow simulators.
 */
public class JdbcArchitectureVisualizerPanel extends JPanel {
    private final User currentAdmin;

    // Simulation Modes
    public enum FlowMode {
        IDLE("Overview / Static Architecture"),
        READ_FLOW("Simulate Read Flow (SELECT query -> ResultSet -> UI Table)"),
        WRITE_FLOW("Simulate Write Flow (Form -> Service -> DAO -> PreparedStatement -> Commit)");

        public final String title;
        FlowMode(String title) { this.title = title; }
    }

    private FlowMode currentFlow = FlowMode.IDLE;
    private int activeStep = -1;
    private Timer animTimer;

    private final List<ArchNode> nodes = new ArrayList<>();
    private ArchNode selectedNode = null;

    private JLabel lblSelectedTitle;
    private JLabel lblSelectedClasses;
    private JTextArea txtSelectedDesc;
    private JLabel lblFlowStatus;

    public JdbcArchitectureVisualizerPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;

        setLayout(new BorderLayout(0, 14));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initNodes();
        initUI();
    }

    private static class ArchNode {
        String id;
        String name;
        String tier;
        String projectClasses;
        String description;
        Rectangle bounds;
        Color color;

        ArchNode(String id, String name, String tier, String projectClasses, String description, Color color) {
            this.id = id;
            this.name = name;
            this.tier = tier;
            this.projectClasses = projectClasses;
            this.description = description;
            this.color = color;
            this.bounds = new Rectangle();
        }
    }

    private void initNodes() {
        nodes.clear();
        nodes.add(new ArchNode("SWING_UI", "Java Swing UI", "Presentation Tier",
                "ui.LoginFrame, ui.DashboardFrame, ui.TransactionPanel, ui.BudgetPanel",
                "Handles user interface presentation, forms, tables (JTable), modal dialogs, and custom Graphics2D charts.",
                new Color(59, 130, 246)));

        nodes.add(new ArchNode("AWT_EVENTS", "AWT Event System", "Event Handling",
                "java.awt.event.ActionListener, MouseListener, DocumentListener",
                "Dispatches user interactions (clicks, keyboard strokes, window events) to UI controllers and background worker threads.",
                new Color(99, 102, 241)));

        nodes.add(new ArchNode("SERVICE", "Java Service Layer", "Business Logic Tier",
                "service.AuthenticationService, TransactionService, BudgetService, SessionService",
                "Enforces business rules, input validation, role-based authorization, PBKDF2 hashing, and ACID transaction boundaries.",
                new Color(16, 185, 129)));

        nodes.add(new ArchNode("DAO", "DAO Data Access Layer", "Data Mapping Tier",
                "dao.UserDAO, dao.TransactionDAO, dao.BudgetDAO, dao.SessionDAO",
                "Encapsulates all SQL statements, manages PreparedStatement parameters, maps relational records into Java domain entities.",
                new Color(245, 158, 11)));

        nodes.add(new ArchNode("JDBC_API", "JDBC Standard API", "Java SE Driver Manager",
                "java.sql.Connection, java.sql.PreparedStatement, java.sql.Statement",
                "Standard Java SE database connectivity interfaces for uniform access across relational DBMS platforms.",
                new Color(139, 92, 246)));

        nodes.add(new ArchNode("DRIVER", "MySQL Connector/J", "Vendor JDBC Driver",
                "com.mysql.cj.jdbc.Driver, com.mysql.cj.jdbc.ConnectionImpl",
                "Translates standard JDBC method calls into MySQL binary network protocol packets transmitted over TCP port 3306.",
                new Color(236, 72, 153)));

        nodes.add(new ArchNode("DATABASE", "MySQL Relational DB", "Persistence Tier",
                "Database Engine (InnoDB), Tables: users, transactions, budgets, audit_logs",
                "Persistent ACID storage engine executing SQL statements, enforcing foreign key constraints, primary keys, and transaction logs.",
                new Color(239, 68, 68)));

        nodes.add(new ArchNode("RESULTSET", "JDBC ResultSet", "Relational Cursor",
                "java.sql.ResultSet, ResultMetadata",
                "Iterates over database row sets, converts column types to Java objects (BigDecimal, LocalDate, String), and returns domain objects.",
                new Color(20, 184, 166)));

        selectedNode = nodes.get(0);
    }

    private void initUI() {
        // TOP CONTROL HEADER
        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JPanel headerTitle = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTitle.setOpaque(false);
        JLabel titleLbl = new JLabel("Interactive JDBC Architecture Visualizer");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Inspect project tiers, trace execution pipelines, and simulate step-by-step Read & Write transactions.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);
        headerTitle.add(titleLbl);
        headerTitle.add(subLbl);
        topHeader.add(headerTitle, BorderLayout.WEST);

        // Simulator Action Toolbar
        JPanel simToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        simToolbar.setOpaque(false);

        CustomButton btnReadSim = new CustomButton("▶ Simulate Read Flow", CustomButton.Style.PRIMARY);
        btnReadSim.setPreferredSize(new Dimension(170, 32));
        btnReadSim.addActionListener(e -> startSimulation(FlowMode.READ_FLOW));

        CustomButton btnWriteSim = new CustomButton("▶ Simulate Write Flow", CustomButton.Style.OUTLINE);
        btnWriteSim.setPreferredSize(new Dimension(170, 32));
        btnWriteSim.addActionListener(e -> startSimulation(FlowMode.WRITE_FLOW));

        CustomButton btnReset = new CustomButton("Reset Flow", CustomButton.Style.SECONDARY);
        btnReset.setPreferredSize(new Dimension(100, 32));
        btnReset.addActionListener(e -> resetSimulation());

        simToolbar.add(btnReadSim);
        simToolbar.add(btnWriteSim);
        simToolbar.add(btnReset);
        topHeader.add(simToolbar, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // CENTER: Interactive Canvas
        DiagramCanvas canvas = new DiagramCanvas();
        add(canvas, BorderLayout.CENTER);

        // BOTTOM: Selected Node Details & Flow Status
        JPanel bottomPanel = new JPanel(new BorderLayout(12, 0));
        bottomPanel.setBackground(ThemeColors.SURFACE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        bottomPanel.setPreferredSize(new Dimension(720, 130));

        JPanel leftDetails = new JPanel(new GridLayout(3, 1, 0, 4));
        leftDetails.setOpaque(false);
        lblSelectedTitle = new JLabel("Selected Node: " + selectedNode.name + " (" + selectedNode.tier + ")");
        lblSelectedTitle.setFont(FontManager.CARD_TITLE);
        lblSelectedTitle.setForeground(ThemeColors.TEXT_MAIN);

        lblSelectedClasses = new JLabel("Project Classes: " + selectedNode.projectClasses);
        lblSelectedClasses.setFont(FontManager.SMALL_BUTTON);
        lblSelectedClasses.setForeground(ThemeColors.PRIMARY);

        lblFlowStatus = new JLabel("Status: Idle • Click any node above to inspect its project role and tier.");
        lblFlowStatus.setFont(FontManager.SMALL);
        lblFlowStatus.setForeground(ThemeColors.TEXT_MUTED);

        leftDetails.add(lblSelectedTitle);
        leftDetails.add(lblSelectedClasses);
        leftDetails.add(lblFlowStatus);
        bottomPanel.add(leftDetails, BorderLayout.WEST);

        txtSelectedDesc = new JTextArea(selectedNode.description);
        txtSelectedDesc.setEditable(false);
        txtSelectedDesc.setLineWrap(true);
        txtSelectedDesc.setWrapStyleWord(true);
        txtSelectedDesc.setFont(FontManager.BODY);
        txtSelectedDesc.setBackground(ThemeColors.SURFACE);
        txtSelectedDesc.setForeground(ThemeColors.TEXT_MUTED);
        txtSelectedDesc.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 0));
        bottomPanel.add(txtSelectedDesc, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void startSimulation(FlowMode mode) {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        currentFlow = mode;
        activeStep = 0;
        updateStatus();

        animTimer = new Timer(1100, e -> {
            activeStep++;
            if (activeStep >= nodes.size()) {
                activeStep = -1;
                lblFlowStatus.setText("Simulation Finished: " + mode.title + " completed successfully.");
                animTimer.stop();
            } else {
                selectedNode = nodes.get(activeStep);
                updateDetails();
                updateStatus();
            }
            repaint();
        });
        animTimer.start();
        repaint();
    }

    private void resetSimulation() {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        currentFlow = FlowMode.IDLE;
        activeStep = -1;
        lblFlowStatus.setText("Status: Simulation reset to idle architecture overview.");
        repaint();
    }

    private void updateStatus() {
        if (activeStep >= 0 && activeStep < nodes.size()) {
            lblFlowStatus.setText("Executing Step " + (activeStep + 1) + " of " + nodes.size() +
                    ": Processing in " + nodes.get(activeStep).name);
        }
    }

    private void updateDetails() {
        if (selectedNode != null) {
            lblSelectedTitle.setText("Selected Node: " + selectedNode.name + " (" + selectedNode.tier + ")");
            lblSelectedClasses.setText("Project Classes: " + selectedNode.projectClasses);
            txtSelectedDesc.setText(selectedNode.description);
        }
    }

    private class DiagramCanvas extends JPanel {
        DiagramCanvas() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (ArchNode n : nodes) {
                        if (n.bounds.contains(e.getPoint())) {
                            selectedNode = n;
                            updateDetails();
                            repaint();
                            break;
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background canvas
            g2.setColor(ThemeColors.SURFACE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
            g2.setColor(ThemeColors.CARD_BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, 12, 12);

            // Layout 8 nodes in a 2-row pipeline (Row 1 left-to-right, Row 2 right-to-left)
            int nodeW = (w - 100) / 4;
            int nodeH = (h - 90) / 2;
            int startX = 30;
            int row1Y = 25;
            int row2Y = row1Y + nodeH + 40;

            for (int i = 0; i < 4; i++) {
                ArchNode n = nodes.get(i);
                n.bounds.setBounds(startX + i * (nodeW + 15), row1Y, nodeW, nodeH);
            }
            // Row 2 nodes (4, 5, 6, 7)
            for (int i = 0; i < 4; i++) {
                ArchNode n = nodes.get(4 + i);
                n.bounds.setBounds(startX + i * (nodeW + 15), row2Y, nodeW, nodeH);
            }

            // Draw flow connecting arrows
            g2.setColor(ThemeColors.CARD_BORDER);
            g2.setStroke(new BasicStroke(2.0f));
            for (int i = 0; i < 3; i++) {
                Rectangle r1 = nodes.get(i).bounds;
                Rectangle r2 = nodes.get(i + 1).bounds;
                g2.drawLine(r1.x + r1.width, r1.y + r1.height / 2, r2.x, r2.y + r2.height / 2);
            }
            // Vertical bridge
            Rectangle r3 = nodes.get(3).bounds;
            Rectangle r4 = nodes.get(4).bounds;
            g2.drawLine(r3.x + r3.width / 2, r3.y + r3.height, r3.x + r3.width / 2, r4.y);

            for (int i = 4; i < 7; i++) {
                Rectangle r1 = nodes.get(i).bounds;
                Rectangle r2 = nodes.get(i + 1).bounds;
                g2.drawLine(r1.x + r1.width, r1.y + r1.height / 2, r2.x, r2.y + r2.height / 2);
            }

            // Render each node card
            for (int i = 0; i < nodes.size(); i++) {
                ArchNode n = nodes.get(i);
                boolean isSelected = (n == selectedNode);
                boolean isStepActive = (i == activeStep);

                Rectangle r = n.bounds;

                // Shadow & Glow
                if (isStepActive) {
                    g2.setColor(new Color(n.color.getRed(), n.color.getGreen(), n.color.getBlue(), 60));
                    g2.fillRoundRect(r.x - 4, r.y - 4, r.width + 8, r.height + 8, 14, 14);
                }

                // Node Body
                g2.setColor(isSelected ? new Color(241, 245, 249) : Color.WHITE);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);

                // Border
                if (isStepActive) {
                    g2.setColor(n.color);
                    g2.setStroke(new BasicStroke(3.0f));
                } else if (isSelected) {
                    g2.setColor(ThemeColors.PRIMARY);
                    g2.setStroke(new BasicStroke(2.0f));
                } else {
                    g2.setColor(ThemeColors.CARD_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

                // Top Accent Strip
                g2.setColor(n.color);
                g2.fillRoundRect(r.x, r.y, r.width, 5, 4, 4);

                // Node Text
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.setFont(FontManager.BODY_BOLD);
                g2.drawString(n.name, r.x + 10, r.y + 24);

                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.setFont(FontManager.SMALL);
                g2.drawString(n.tier, r.x + 10, r.y + 40);

                // Node status pill
                g2.setColor(isStepActive ? ThemeColors.INCOME : new Color(226, 232, 240));
                g2.fillRoundRect(r.x + 10, r.y + r.height - 22, 60, 14, 8, 8);
                g2.setColor(isStepActive ? Color.WHITE : ThemeColors.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.drawString(isStepActive ? "ACTIVE" : "READY", r.x + 18, r.y + r.height - 11);
            }

            g2.dispose();
        }
    }
}
