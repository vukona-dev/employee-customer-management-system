package gui;

import service.ManagementService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel dedicated to displaying summary statistics and analytics.
 * Fetches calculated data from the ManagementService.
 */
public class AnalyticsPanel extends JPanel {

    private final ManagementService managementService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    // --- Output Labels ---
    private JLabel avgAgeLabel, avgSalaryLabel;
    private JLabel goldCountLabel, silverCountLabel, bronzeCountLabel;

    public AnalyticsPanel(ManagementService service) {
        this.managementService = service;
        setLayout(new BorderLayout(10, 10));

        initComponents();
        refreshData(); // Load data on initialization
    }

    private void initComponents() {
        // --- Center Panel (Display Metrics) ---
        JPanel metricsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Key System Metrics"));

        // 1. Average Age
        avgAgeLabel = new JLabel("N/A");
        metricsPanel.add(new JLabel("1. Average Age of People (Employees + Customers):"));
        metricsPanel.add(avgAgeLabel);

        // 2. Average Salary
        avgSalaryLabel = new JLabel("N/A");
        metricsPanel.add(new JLabel("2. Average Employee Salary:"));
        metricsPanel.add(avgSalaryLabel);

        // --- Separator ---
        metricsPanel.add(new JSeparator());
        metricsPanel.add(new JSeparator());

        // 3. Membership Breakdown (Gold)
        goldCountLabel = new JLabel("N/A");
        metricsPanel.add(new JLabel("3. Gold Membership Count:"));
        metricsPanel.add(goldCountLabel);

        // 4. Membership Breakdown (Silver)
        silverCountLabel = new JLabel("N/A");
        metricsPanel.add(new JLabel("4. Silver Membership Count:"));
        metricsPanel.add(silverCountLabel);

        // 5. Membership Breakdown (Bronze)
        bronzeCountLabel = new JLabel("N/A");
        metricsPanel.add(new JLabel("5. Bronze Membership Count:"));
        metricsPanel.add(bronzeCountLabel);

        add(metricsPanel, BorderLayout.CENTER);

        // --- South Panel (Control Button) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Analytics");

        // Add action listener to the refresh button
        refreshButton.addActionListener(e -> refreshData());

        controlPanel.add(refreshButton);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Retrieves all calculated data from the ManagementService and updates the UI labels.
     */
    public void refreshData() {
        try {
            // Calculate Average Age (Employees + Customers)
            double avgAge = managementService.getAverageAge();
            avgAgeLabel.setText(String.format("%.1f years", avgAge));

            // Calculate Average Salary (Employees only)
            double avgSalary = managementService.getAverageSalary();
            // Use currency format for a clean display
            avgSalaryLabel.setText(currencyFormat.format(avgSalary));

            // Calculate Membership Counts
            long goldCount = managementService.getMembershipCount("Gold");
            long silverCount = managementService.getMembershipCount("Silver");
            long bronzeCount = managementService.getMembershipCount("Bronze");

            goldCountLabel.setText(String.valueOf(goldCount));
            silverCountLabel.setText(String.valueOf(silverCount));
            bronzeCountLabel.setText(String.valueOf(bronzeCount));

        } catch (Exception e) {
            System.err.println("Error refreshing analytics data: " + e.getMessage());
            // In a production app, a clearer error message would be displayed to the user
            JOptionPane.showMessageDialog(this, "Could not load analytics. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}