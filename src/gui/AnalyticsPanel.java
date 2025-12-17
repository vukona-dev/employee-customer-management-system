package gui;

import org.jfree.chart.renderer.category.BarRenderer;
import service.ManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.text.NumberFormat;
import java.util.Locale;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import java.text.DecimalFormat;

// --- JFreeChart Imports ---
// (Ensure JFreeChart and JCommon JARs are in your project's classpath)
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
// --------------------------

/**
 * Panel dedicated to displaying non-redundant, in-depth analytics and reports.
 * It features a metrics table and two different chart visualizations.
 */
public class AnalyticsPanel extends JPanel {
    // Custom format for South African Rand (R)
    private static final NumberFormat RAND_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));

    private final ManagementService managementService;
    private JPanel metricsTablePanel;
    private JPanel visualizationsPanel;

    public AnalyticsPanel(ManagementService service) {
        this.managementService = service;
        setLayout(new BorderLayout(10, 10));

        initComponents();
        refreshAnalyticsContent(); // Load data on creation
    }

    private void initComponents() {
        // --- 1. Top Metrics/Table Panel ---
        metricsTablePanel = new JPanel(new BorderLayout());
        metricsTablePanel.setBorder(BorderFactory.createTitledBorder("Average Salary by Job Title (Detailed Metrics)"));
        add(metricsTablePanel, BorderLayout.NORTH);

        // --- 2. Center Visualizations Panel ---
        // Uses GridLayout(1 row, 2 columns) to place the two charts side-by-side
        visualizationsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        visualizationsPanel.setBorder(BorderFactory.createTitledBorder("Key Distributions"));
        add(visualizationsPanel, BorderLayout.CENTER);
    }

    /**
     * Public method to load all analytics data and refresh the UI.
     * Called upon panel initialization and when the tab is switched to.
     */
    public void refreshAnalyticsContent() {
        loadJobTitleMetricsTable();

        // Clear previous charts and load new ones
        visualizationsPanel.removeAll();
        visualizationsPanel.add(createEmployeeCountChartPanel());
        visualizationsPanel.add(createCustomerMembershipChartPanel());

        // Must call revalidate/repaint to redraw the panels
        revalidate();
        repaint();
    }

    /**
     * Loads and displays text metrics (Average Salary) in a formatted JTable view.
     */
    private void loadJobTitleMetricsTable() {
        metricsTablePanel.removeAll();

        // Get data from the service layer (which delegates to AnalyticsEngine)
        Map<String, Double> avgSalaries = managementService.getAverageSalaryByJobTitle();

        // Use TreeMap for sorted job titles for better presentation
        Map<String, Double> sortedSalaries = new TreeMap<>(avgSalaries);

        if (sortedSalaries.isEmpty()) {
            metricsTablePanel.add(new JLabel("No employee data available for salary analysis."), BorderLayout.CENTER);
            return;
        }

        // 1. Prepare Table Data
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Job Title");
        columnNames.add("Average Salary");

        Vector<Vector<Object>> rowData = new Vector<>();

        for (Map.Entry<String, Double> entry : sortedSalaries.entrySet()) {
            Vector<Object> row = new Vector<>();
            row.add(entry.getKey());
            // Format salary for currency display
            row.add(RAND_FORMATTER.format(entry.getValue()));
            rowData.add(row);
        }

        // 2. Create the JTable
        DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames);
        JTable table = new JTable(tableModel);
        table.setEnabled(false); // Make it read-only
        table.getTableHeader().setReorderingAllowed(false);

        // 3. Add to panel
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 150)); // Fixed size for the table area

        metricsTablePanel.add(scrollPane, BorderLayout.CENTER);
        metricsTablePanel.revalidate();
        metricsTablePanel.repaint();
    }

    /**
     * Generates and returns a ChartPanel for the Employee Count Bar Chart.
     *
     */
    private ChartPanel createEmployeeCountChartPanel() {
        Map<String, Integer> jobCounts = managementService.getEmployeeCountByJobTitle();

        // 1. Create a JFreeChart Dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : jobCounts.entrySet()) {
            // Adds: (Value, RowKey 'Series', ColumnKey 'Category')
            dataset.addValue(entry.getValue(), "Employees", entry.getKey());
        }

        // 2. Create the Bar Chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Employee Count by Job Title", // Chart title
                "Job Title",                   // X-axis label
                "Number of Employees",         // Y-axis label
                dataset,                       // Data
                PlotOrientation.VERTICAL,
                false,                         // Hide legend
                true,                          // Show tooltips
                false                          // Hide URLs
        );

        // --- CRITICAL FIX: Ensure Y-axis displays only integers ---
        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        // Force the Y-axis (Range Axis) to use standard integer tick units
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Optional: Ensure the Y-axis starts exactly at 0.0, common for count data
        rangeAxis.setLowerBound(0.0);

        // 1. Get the BarRenderer from the plot
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // 2. Set the color for all bars (Series 0) to a new color (e.g., Blue)
        Color customBarColor = new Color(0, 102, 204); // A nice medium blue

        // Sets the color for the entire series (all bars in this case)
        renderer.setSeriesPaint(0, customBarColor);

        // 3. OPTIONAL: Customize bar appearance (removes the default 3D effect border)
        renderer.setDrawBarOutline(false);
        // ---------------------------------------

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return chartPanel;
    }

    /**
     * Generates and returns a ChartPanel for the Customer Membership Level Pie Chart.
     *
     */
    private ChartPanel createCustomerMembershipChartPanel() {
        Map<String, Integer> membershipCounts = managementService.getCustomerCountByMembershipLevel();

        // 1. Create a JFreeChart Dataset
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> entry : membershipCounts.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        // 2. Create the Pie Chart
        JFreeChart chart = ChartFactory.createPieChart(
                "Customer Membership Level Distribution",
                dataset,
                true,                          // Show legend
                true,                          // Show tooltips
                false                          // Hide URLs
        );

        PiePlot plot = (PiePlot) chart.getPlot();

        // 1. Set Custom Colors
        // Define colors that match the membership names (using hex for accuracy)
        plot.setSectionPaint("Platinum", new Color(229, 228, 226)); // Light Grey/Platinum
        plot.setSectionPaint("Gold", new Color(255, 215, 0));       // Gold
        plot.setSectionPaint("Silver", new Color(182, 182, 182));   // Silver
        plot.setSectionPaint("Bronze", new Color(168, 107, 38));    // Bronze

        // 2. Set Percentage Labels
        // Define the format to show value (e.g., 25.0%)
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{2}", new DecimalFormat("0.0%"), new DecimalFormat("0.0%")
        ));

        // 3. Optional: Customize the plot appearance
        plot.setNoDataMessage("No membership data to display.");
        plot.setSimpleLabels(true); // Ensures labels are drawn neatly
        plot.setOutlineVisible(false); // Clean up the border

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return chartPanel;
    }
}