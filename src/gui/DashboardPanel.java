package gui;

import service.ManagementService;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redesigned Dashboard: Focused Quadrant Manager.
 * Final version with critical fixes for:
 * 1. Text overlap (increased vertical spacing further).
 * 2. Dynamic status text now correctly shows "Full Access," "Read Only," or "Restricted"
 * based on the user's role and the tab's enabled status.
 */
public class DashboardPanel extends JPanel {

    private final ManagementService service;
    private final MainFrame mainFrame;

    // --- Theme Colors ---
    private static final Color BG_MAIN = new Color(225, 228, 228);
    private static final Color BG_MODULE = new Color(186, 185, 185, 255);
    private static final Color BORDER_LIGHT = new Color(0, 98, 255);
    private static final Color ACCENT_PRIMARY = new Color(40, 100, 180);
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color BUTTON_DENIED_BG = Color.DARK_GRAY.darker();

    // --- Dynamic UI Fields ---
    private JLabel welcomeMessageLabel;
    private JPanel quadrantContainer;

    // Labels needed for dynamic status update
    private JLabel[] statusLabels = new JLabel[4];
    private JLabel[] accessLabels = new JLabel[4];


    public DashboardPanel(ManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setBackground(BG_MAIN);
        setLayout(new BorderLayout(40, 40));
        setBorder(new EmptyBorder(50, 70, 50, 70));

        initComponents();
    }

    private void initComponents() {

        add(createWelcomeHeader(), BorderLayout.NORTH);
        quadrantContainer = createQuadrantContainer();
        add(quadrantContainer, BorderLayout.CENTER);

        JLabel systemStatusLabel = new JLabel("Enterprise System Status: Online | Core Modules: 4/4 Operational", JLabel.CENTER);
        systemStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        systemStatusLabel.setForeground(Color.GRAY);
        add(systemStatusLabel, BorderLayout.SOUTH);
    }

    private JPanel createWelcomeHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_MAIN);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        welcomeMessageLabel = new JLabel("Initializing...", SwingConstants.LEFT);
        header.add(welcomeMessageLabel, BorderLayout.CENTER);

        return header;
    }

    private JPanel createQuadrantContainer() {
        JPanel container = new JPanel(new GridLayout(2, 2, 40, 40));
        container.setBackground(BG_MAIN);

        // Modules added sequentially to correspond with tab indices 1-4
        container.add(createModuleQuadrant("Employee Management", "HR Directory", 1, new Color(50, 150, 200), 0));
        container.add(createModuleQuadrant("Customer Portfolio", "Client Data", 2, new Color(100, 180, 100), 1));
        container.add(createModuleQuadrant("Analytics & Reporting", "Data Insights", 3, new Color(200, 150, 60), 2));
        container.add(createModuleQuadrant("System Administration", "User Control", 4, new Color(180, 80, 80), 3));

        return container;
    }

    /**
     * Creates a self-contained quadrant module, storing the dynamic labels in arrays.
     */
    private JPanel createModuleQuadrant(String title, String subtitle, int tabIndex, Color statusColor, int arrayIndex) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MODULE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // --- 1. Module Header (Top) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_MODULE);

        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY.darker());

        header.add(titleLabel, BorderLayout.WEST);
        header.add(subtitleLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // --- 2. Status Content (Center) ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_MODULE);
        content.setBorder(new EmptyBorder(15, 0, 15, 0));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Initialize dynamic labels
        statusLabels[arrayIndex] = new JLabel("Status: Initializing...");
        statusLabels[arrayIndex].setFont(new Font("Arial", Font.BOLD, 14));
        statusLabels[arrayIndex].setForeground(TEXT_DARK);
        statusLabels[arrayIndex].setAlignmentX(Component.LEFT_ALIGNMENT);

        // Simplified Last Accessed text
        accessLabels[arrayIndex] = new JLabel("Last accessed: ---");
        accessLabels[arrayIndex].setFont(new Font("Arial", Font.ITALIC, 11));
        accessLabels[arrayIndex].setForeground(Color.GRAY);
        accessLabels[arrayIndex].setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components with increased vertical strut for robust spacing
        content.add(statusLabels[arrayIndex]);
        // FIX: Increased strut to 10 pixels to GUARANTEE text separation
        content.add(Box.createVerticalStrut(10));
        content.add(accessLabels[arrayIndex]);
        content.add(Box.createVerticalGlue());

        panel.add(content, BorderLayout.CENTER);

        // --- 3. Action Button (Bottom) ---
        JButton actionButton = new JButton("Open Module >");
        actionButton.setFont(new Font("Arial", Font.BOLD, 12));
        actionButton.setBackground(ACCENT_PRIMARY);
        actionButton.setForeground(Color.WHITE);
        actionButton.setFocusPainted(false);
        actionButton.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(actionButton, BorderLayout.SOUTH);

        // Action listener
        actionButton.addActionListener(e -> {
            if (mainFrame.getTabbedPane().isEnabledAt(tabIndex)) {
                mainFrame.getTabbedPane().setSelectedIndex(tabIndex);
            } else {
                mainFrame.updateStatusBar("Access Denied: Your role (" + service.getAuthService().getActiveUser().getRole() + ") cannot access this feature.", false);
            }
        });

        // Hover effect for the entire panel
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(BG_MODULE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(BG_MODULE);
            }
        });

        return panel;
    }

    /**
     * Helper to determine the granular access status text for a module.
     */
    private String getAccessStatusText(int tabIndex, String role) {
        // Tab Index: 1=Employee, 2=Customer, 3=Analytics, 4=Admin
        if (mainFrame.getTabbedPane().isEnabledAt(tabIndex)) {
            // Module is accessible (either Full or Read Only)
            if (role.equals("Admin") || role.equals("Manager")) {
                // Admins and Managers typically get full access to everything they can see.
                return "Status: Full Access";
            } else if (role.equals("HR Specialist") && tabIndex == 1) {
                // HR Specialist has full access to Employee Management
                return "Status: Full Access";
            } else {
                // Assume any other non-admin/non-manager access is Read Only by default
                // for modules outside their primary expertise (e.g., HR viewing Customer)
                return "Status: Read Only";
            }
        } else {
            // Module is entirely disabled for this role
            return "Status: Restricted";
        }
    }

    // =========================================================
    // Dynamic Content & RBAC Methods
    // =========================================================

    public void refreshDashboardContent() {
        User user = service.getAuthService().getActiveUser();

        if (user == null) {
            welcomeMessageLabel.setText("<html><h1 style='color: red;'>FATAL ERROR: No User Session</h1></html>");
            return;
        }

        String role = user.getRole();
        LocalDateTime now = LocalDateTime.now();

        // 1. Build the Welcome Header Message
        String welcomeMessageHTML =
                "<html>"
                        + "<span style='font-size: 28pt; color: #1a1a1a;'>Module Control Center</span><br>"
                        + "<span style='font-size: 14pt; color: #555; margin-top: 5px;'>"
                        + "Logged in as <b>" + user.getUsername() + "</b> | Role: " + role + " | Last Login: "
                        + now.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a"))
                        + "</span>"
                        + "</html>";

        welcomeMessageLabel.setText(welcomeMessageHTML);

        // 2. Apply RBAC and Dynamic Status Update
        Component[] components = quadrantContainer.getComponents();
        int tabIndex = 1;

        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];

            if (comp instanceof JPanel) {
                JPanel quadrantPanel = (JPanel) comp;
                JButton actionButton = null;

                // Safely find the JButton
                for (Component innerComp : quadrantPanel.getComponents()) {
                    if (innerComp instanceof JButton) {
                        actionButton = (JButton) innerComp;
                        break;
                    }
                }

                if (actionButton != null) {
                    boolean enabled = mainFrame.getTabbedPane().isEnabledAt(tabIndex);

                    // Get the dynamic status text
                    String statusText = getAccessStatusText(tabIndex, role);

                    // Update the status and last accessed labels
                    statusLabels[i].setText(statusText);
                    accessLabels[i].setText("Last accessed: " + (enabled ? now.format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A"));

                    actionButton.setEnabled(enabled);

                    // Visually mark restricted modules
                    if (!enabled) {
                        actionButton.setText("Access Denied");
                        actionButton.setBackground(BUTTON_DENIED_BG);
                        actionButton.setForeground(Color.WHITE);
                    } else {
                        actionButton.setText("Open Module >");
                        actionButton.setBackground(ACCENT_PRIMARY);
                        actionButton.setForeground(Color.WHITE);
                    }
                }

                tabIndex++;
            }
        }

        revalidate();
        repaint();
    }
}