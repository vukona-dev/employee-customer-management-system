package gui;

import service.ManagementService;
import model.User;
import javax.swing.*;
import java.awt.*;
import gui.DashboardPanel;

/**
 * The main application window for the ECMS.
 * Uses a JTabbedPane to host the Dashboard and other management panels.
 */
public class MainFrame extends JFrame {

    // Service layer instance (Used by all panels to interact with business logic)
    private final ManagementService managementService;

    // --- NEW FIELD ---
    private User activeUser; // Stores the currently logged-in user

    // GUI Components
    private JTabbedPane tabbedPane;
    private JLabel statusBar;

    // --- NEW PANEL DECLARATION ---
    private DashboardPanel dashboardPanel;

    private EmployeePanel employeePanel;
    private CustomerPanel customerPanel;
    private AnalyticsPanel analyticsPanel;
    private UserManagementPanel userManagementPanel;

    // --- RBAC Controlled Components (Examples) ---
    private JMenuItem logoutMenuItem;

    public MainFrame(ManagementService service) {
        this.managementService = service;

        // --- 1. Basic Frame Setup ---
        setTitle("ECMS - Employee & Customer Management System (Logged Out)");
        setSize(1000, 700); // Standard desktop application size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the screen

        // --- 2. Initialize Components ---
        initComponents();

        // --- 3. Final Display ---
        // CRUCIAL CHANGE: The frame is NOT visible at startup. LoginFrame handles visibility.
        setVisible(false);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Initialize the sub-panels (passing the ManagementService to them)
        // 1. DASHBOARD PANEL (The new default home screen)
        dashboardPanel = new DashboardPanel(managementService, this);

        employeePanel = new EmployeePanel(managementService, this);
        customerPanel = new CustomerPanel(managementService, this);
        analyticsPanel = new AnalyticsPanel(managementService);
        userManagementPanel = new UserManagementPanel(managementService, this);

        // Add the panels to the tabbed pane in the desired order
        // Tab 0: Dashboard (The new default)
        tabbedPane.addTab("Dashboard", createIcon("home"), dashboardPanel);

        // Tab 1: Employee Management
        tabbedPane.addTab("Employee Management", createIcon("employee"), employeePanel);

        // Tab 2: Customer Management
        tabbedPane.addTab("Customer Management", createIcon("customer"), customerPanel);

        // Tab 3: Analytics & Reporting
        tabbedPane.addTab("Analytics & Reporting", createIcon("analytics"), analyticsPanel);

        // Tab 4: User Management (Admin Only)
        tabbedPane.addTab("User Management", createIcon("admin"), userManagementPanel);

        // Add the tabbed pane to the frame
        add(tabbedPane, BorderLayout.CENTER);

        // Add the Status/Notification Bar (Requirement 1)
        // Add the Status/Notification Bar (Requirement 1)
        statusBar = new JLabel(" ECMS Application Ready (Version 1.0) ");
        statusBar.setOpaque(true); // Required to show the background color
        statusBar.setBackground(Color.LIGHT_GRAY);
        statusBar.setForeground(Color.BLACK);
        statusBar.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusBar, BorderLayout.SOUTH);

        // Add the Change Listener to refresh data ---
        tabbedPane.addChangeListener(e -> {
            // Check if the selected component is the AnalyticsPanel
            if (tabbedPane.getSelectedComponent() == analyticsPanel) {
                // CORRECTED METHOD CALL: Uses the name defined in AnalyticsPanel
                analyticsPanel.refreshAnalyticsContent();
            }
            // Optional: You may want to refresh the Dashboard when switching back to it
            else if (tabbedPane.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refreshDashboardContent();
            }
        });

        // --- NEW: Setup the Menu Bar (for Logout and RBAC) ---
        setupMenuBar();
    }

    // --- Add this entire block to the end of MainFrame.java ---

// =========================================================
// STATUS BAR IMPLEMENTATION (Requirement 1)
// =========================================================

    /**
     * Updates the message and color of the notification bar.
     * @param message The text to display.
     * @param isSuccess True for success (green), false for failure/error (red).
     */
    public void updateStatusBar(String message, boolean isSuccess) {
        if (statusBar == null) {
            System.err.println("StatusBar not initialized in MainFrame.");
            return;
        }

        // Set the new message
        statusBar.setText(" " + message + " ");

        // Set color based on status
        if (isSuccess) {
            statusBar.setBackground(new Color(153, 255, 153)); // Light Green
        } else {
            statusBar.setBackground(new Color(255, 153, 153)); // Light Red (The color you referenced earlier)
        }
        statusBar.setForeground(Color.BLACK);
    }

    /**
     * Resets the status bar to its default "Application Ready" state.
     */
    public void resetStatusBar() {
        if (statusBar == null) return;
        statusBar.setText(" ECMS Application Ready (Version 1.0) ");
        statusBar.setBackground(Color.LIGHT_GRAY);
        statusBar.setForeground(Color.BLACK);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu appMenu = new JMenu("Application");

        logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> handleLogout());

        appMenu.add(logoutMenuItem);
        menuBar.add(appMenu);
        setJMenuBar(menuBar);

        // Initial state: Disable components until login success
        applyRoleBasedAccessControl();
    }


    /**
     * Placeholder method for creating icons (Assumes icon files are in resources/icons)
     */
    private ImageIcon createIcon(String name) {
        // In a real application, you would load icons from your resources here.
        // Example: return new ImageIcon(getClass().getResource("/icons/" + name + ".png"));
        return null;
    }

    // ----------------------------------------------------------------------
    // --- NEW METHODS FOR AUTHENTICATION & RBAC ---
    // ----------------------------------------------------------------------

    /**
     * Sets the active user after successful login and applies security controls.
     */

    public void setActiveUser(User user) {
        this.activeUser = user;

        // CRITICAL NEW LINE: Tell the AuthenticationService who the active user is.
        managementService.getAuthService().setActiveUser(user);

        System.out.println("MainFrame Active User Set: " + user.getUsername() + ", Role: " + user.getRole());

        this.setTitle("ECMS - Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");

        tabbedPane.setSelectedIndex(0);

        if (dashboardPanel != null) {
            dashboardPanel.refreshDashboardContent();
        }

        applyRoleBasedAccessControl();

        // --- NEW: Apply fine-grained controls to operational panels now that the user is logged in ---
        if (employeePanel != null) {
            employeePanel.refreshAccessControls();
        }
        if (customerPanel != null) {
            customerPanel.refreshAccessControls();
        }
    }

    /**
     * Triggers a refresh of the dashboard metrics.
     * Called by sub-panels (Employee/Customer) after a successful save or delete operation.
     */
    public void refreshMetricsDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.refreshDashboardContent();
        }
    }

    /**
     * Applies Role-Based Access Control (RBAC) to UI elements.
     * This method enables/disables features based on the active user's role.
     */
    private void applyRoleBasedAccessControl() {
        boolean isLoggedIn = (activeUser != null);
        String role = isLoggedIn ? activeUser.getRole() : "";

        // 1. Update Title and Logout Button State
        if (isLoggedIn) {
            logoutMenuItem.setEnabled(true);
        } else {
            this.setTitle("ECMS - Employee & Customer Management System (Logged Out)");
            logoutMenuItem.setEnabled(false);
        }

        // 2. Control Tabs and Panels based on Role
        // Dashboard (Index 0) is always accessible if logged in.
        tabbedPane.setEnabledAt(0, isLoggedIn); // Dashboard

        // Employee Management (Index 1) Access: Admin, Manager, HR Specialist, Data Analyst (Read Only)
        // All these roles need to see the Employee Panel for their function.
        boolean canViewEmployees = role.equals("Admin") || role.equals("Manager") || role.equals("HR Specialist") || role.equals("Data Analyst");
        tabbedPane.setEnabledAt(1, canViewEmployees); // Employee Tab

        // Customer Management (Index 2) Access: Admin, Manager, Cust. Service Agent, Data Analyst (Read Only)
        boolean canViewCustomers = role.equals("Admin") || role.equals("Manager") || role.equals("Customer Service Agent") || role.equals("Data Analyst");
        tabbedPane.setEnabledAt(2, canViewCustomers); // Customer Tab

        // Analytics & Reporting (Index 3) Access: Admin, Manager, Data Analyst, HR Specialist (HR Specialist will see limited data, but the tab is enabled)
        boolean canViewAnalytics = role.equals("Admin") || role.equals("Manager") || role.equals("Data Analyst") || role.equals("HR Specialist");
        tabbedPane.setEnabledAt(3, canViewAnalytics); // Analytics Tab

        // User Management (Index 4) is restricted to Admin only
        boolean canManageUsers = role.equals("Admin");
        tabbedPane.setEnabledAt(4, canManageUsers);

        // 3. Ensure the selected tab is accessible
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (!tabbedPane.isEnabledAt(selectedIndex)) {
            // If the currently selected tab is disabled (e.g., if a manager logs out),
            // switch them to a safe, accessible tab (the Dashboard).
            tabbedPane.setSelectedIndex(0);
        }
    }

    /**
     * Handles the user logging out.
     */
    public void handleLogout() {
        // Clear the user, reset controls, and re-launch login screen
        this.activeUser = null;
        applyRoleBasedAccessControl();

        this.setVisible(false); // Hide the MainFrame

        // Start a new LoginFrame (passing the same MainFrame instance for re-use)
        // NOTE: You must ensure your LoginFrame constructor accepts a MainFrame argument.
        // Assuming you have a LoginFrame class available:
        // new LoginFrame(this).setVisible(true);
        System.out.println("User logged out successfully.");
    }

    // ----------------------------------------------------------------------
    // --- MAIN METHOD MODIFICATION (for testing purposes) ---
    // ----------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // NOTE: This Main method is for testing only.
            ManagementService service = new ManagementService();
            // Start the main frame (It will be hidden until login)
            new MainFrame(service);
        });
    }
    /**
     * Public getter to allow panels (like Dashboard) to reference the main tab container
     * for navigation purposes (e.g., Quick Actions).
     * @return The JTabbedPane instance used for the application's main navigation.
     */
    public JTabbedPane getTabbedPane() {
        // Assuming your JTabbedPane field is named 'tabbedPane'
        return this.tabbedPane;
    }
}