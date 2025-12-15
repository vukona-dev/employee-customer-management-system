package gui;

import service.ManagementService;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window for the ECMS.
 * Uses a JTabbedPane to host the Employee and Customer management panels.
 */
public class MainFrame extends JFrame {

    // Service layer instance (Used by all panels to interact with business logic)
    private final ManagementService managementService;

    // GUI Components
    private JTabbedPane tabbedPane;
    private EmployeePanel employeePanel;
    private CustomerPanel customerPanel;
    private AnalyticsPanel analyticsPanel;

    public MainFrame(ManagementService service) {
        this.managementService = service;

        // --- 1. Basic Frame Setup ---
        setTitle("ECMS - Employee & Customer Management System");
        setSize(1000, 700); // Standard desktop application size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the screen

        // --- 2. Initialize Components ---
        initComponents();

        // --- 3. Final Display ---
        setVisible(true);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Initialize the sub-panels (passing the ManagementService to them)
        employeePanel = new EmployeePanel(managementService);
        customerPanel = new CustomerPanel(managementService);
        analyticsPanel = new AnalyticsPanel(managementService);

        // Add the panels to the tabbed pane
        tabbedPane.addTab("Employee Management", createIcon("employee"), employeePanel);
        tabbedPane.addTab("Customer Management", createIcon("customer"), customerPanel);
        tabbedPane.addTab("Analytics & Reporting", createIcon("analytics"), analyticsPanel);

        // Add the tabbed pane to the frame
        add(tabbedPane, BorderLayout.CENTER);

        // Add a simple footer/status bar
        add(new JLabel(" Ready | DB Connected | App Version 1.0", SwingConstants.CENTER), BorderLayout.SOUTH);

        // Add the Change Listener to refresh data ---
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == analyticsPanel) {
                // When the tab is switched to Analytics, refresh the data
                analyticsPanel.refreshData();
            }
        });
    }

    /**
     * Placeholder method for creating icons (Assumes icon files are in resources/icons)
     */
    private ImageIcon createIcon(String name) {
        // You would load icons from the resources/icons/ directory here.
        // For now, we return null to avoid compile errors if the icons aren't created yet.
        return null;
    }

    // Main method to run the application
    public static void main(String[] args) {
        // Swing applications should start on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Initialize the Service layer
            ManagementService service = new ManagementService();

            // Start the main frame
            new MainFrame(service);
        });
    }
}