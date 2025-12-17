package gui;

import model.Customer;
import service.ManagementService;
import util.CustomExceptions;
import java.time.format.DateTimeParseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.io.File;

// ----------------------------------------------------------------------
// CRITICAL NEW IMPORT for the calendar component
import com.toedter.calendar.JDateChooser;
// ----------------------------------------------------------------------

/**
 * Panel dedicated to the management of Customer entities (CRUD UI).
 */
public class CustomerPanel extends JPanel {

    private final ManagementService managementService;
    private final MainFrame mainFrame;

    // --- GUI Components (Form) ---
    private JTextField idField, nameField, ageField;
    private JComboBox<String> membershipLevelComboBox;

    // MODIFIED: Replaced text field/button with JDateChooser
    private JDateChooser lastPurchaseDateChooser;

    private JButton saveButton, deleteButton;
    private JButton exportButton;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    private static final String[] MEMBERSHIP_LEVELS = {"Bronze", "Silver", "Gold", "Platinum"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CustomerPanel(ManagementService service, MainFrame mainFrame) {
        this.managementService = service;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadCustomerData();

        //applyRoleBasedAccessControl();
    }

    private void initComponents() {
        // --- 1. Top Panel (Form and Controls) ---
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // --- 2. Center Panel (Table View) ---
        JScrollPane tableScrollPane = createTablePanel();
        add(tableScrollPane, BorderLayout.CENTER);

        // --- 3. Button Actions ---
        setupActions();
    }

    // =========================================================
    // UI Creation Methods
    // =========================================================

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Customer Details"));

        // Input Fields Panel (GridLayout for labels and fields)
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        // ID Field
        idField = new JTextField();
        idField.setEditable(false);
        idField.setBackground(Color.LIGHT_GRAY);
        fieldsPanel.add(new JLabel("ID (Auto):"));
        fieldsPanel.add(idField);

        // Name and Age Fields
        nameField = new JTextField();
        fieldsPanel.add(new JLabel("Name:"));
        fieldsPanel.add(nameField);

        ageField = new JTextField();
        fieldsPanel.add(new JLabel("Age:"));
        fieldsPanel.add(ageField);

        // JComboBox for Membership Level
        membershipLevelComboBox = new JComboBox<>(MEMBERSHIP_LEVELS);
        fieldsPanel.add(new JLabel("Membership Level:"));
        fieldsPanel.add(membershipLevelComboBox);

        // JDateChooser for Date Selection
        lastPurchaseDateChooser = new JDateChooser();
        lastPurchaseDateChooser.setDateFormatString("yyyy-MM-dd"); // Format the display
        fieldsPanel.add(new JLabel("Last Purchase Date:"));
        fieldsPanel.add(lastPurchaseDateChooser); // Add the calendar component

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // B. Control Buttons Panel (Unchanged)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save Customer (Create/Update)");
        deleteButton = new JButton("Delete Selected Customer");
        JButton newButton = new JButton("New/Clear Form");

        exportButton = new JButton("Export to CSV");

        buttonPanel.add(newButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Setup initial state
        newButton.addActionListener(e -> clearForm());
        deleteButton.setEnabled(false);
        exportButton.addActionListener(this::handleExportAction);

        return panel;
    }

    // (handleDateSelectAction is now OBSOLETE as JDateChooser handles it)

    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "Name", "Age", "Membership", "Last Purchase Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        customerTable = new JTable(tableModel);

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                populateFormFromTable(customerTable.getSelectedRow());

                // Only enable the delete button if the user has CRUD access
                String role = managementService.getAuthService().getActiveUser().getRole();
                boolean hasCrudAccess = role.equals("Admin") || role.equals("Manager") || role.equals("Customer Service Agent");

                deleteButton.setEnabled(hasCrudAccess); // <-- RBAC CHECK APPLIED HERE
            }
        });

        return new JScrollPane(customerTable);
    }

    // =========================================================
    // Data Loading and Mapping Methods
    // =========================================================

    private void loadCustomerData() {
        tableModel.setRowCount(0);

        try { // <-- START Robustness: Try-catch for DAO call
            List<Customer> customers = managementService.getAllCustomers();

            for (Customer cust : customers) {
                Object[] row = new Object[]{
                        cust.getId(),
                        cust.getName(),
                        cust.getAge(),
                        cust.getMembershipLevel(),
                        // Ensure date is handled safely, even if null in model
                        cust.getLastPurchaseDate() != null ? cust.getLastPurchaseDate().toString() : "N/A"
                };
                tableModel.addRow(row);
            }
            mainFrame.updateStatusBar("Customer table loaded successfully.", true); // <-- Status Bar Update (Success)

        } catch (Exception ex) { // Catch DataAccessException or other issues
            mainFrame.updateStatusBar("Error loading customer data: " + ex.getMessage(), false); // <-- Status Bar Update (Error)
        }
    }

    private void populateFormFromTable(int rowIndex) {
        idField.setText(tableModel.getValueAt(rowIndex, 0).toString());
        nameField.setText(tableModel.getValueAt(rowIndex, 1).toString());
        ageField.setText(tableModel.getValueAt(rowIndex, 2).toString());

        membershipLevelComboBox.setSelectedItem(tableModel.getValueAt(rowIndex, 3).toString());

        // CRITICAL MODIFICATION: Set JDateChooser from String
        String dateString = tableModel.getValueAt(rowIndex, 4).toString();
        try {
            LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER);
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            lastPurchaseDateChooser.setDate(date);
        } catch (DateTimeParseException e) {
            lastPurchaseDateChooser.setDate(null);
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");

        membershipLevelComboBox.setSelectedIndex(0);

        // CRITICAL MODIFICATION: Clear JDateChooser
        lastPurchaseDateChooser.setDate(null);

        deleteButton.setEnabled(false);
        customerTable.clearSelection();
        mainFrame.resetStatusBar(); // <-- Status Bar Update (Reset)
    }

    // =========================================================
    // Action Handlers (CRUD Logic)
    // =========================================================

    private void setupActions() {
        saveButton.addActionListener(this::handleSaveAction);
        deleteButton.addActionListener(this::handleDeleteAction);
    }

    private void handleSaveAction(ActionEvent e) {
        String id = idField.getText().isEmpty() ? UUID.randomUUID().toString() : idField.getText();
        String name = nameField.getText();

        try {
            int age = Integer.parseInt(ageField.getText());
            String membershipLevel = (String) membershipLevelComboBox.getSelectedItem();

            // CRITICAL MODIFICATION: Get Date from JDateChooser
            Date selectedDate = lastPurchaseDateChooser.getDate();
            if (selectedDate == null) {
                throw new IllegalArgumentException("Last Purchase Date must be selected.");
            }
            LocalDate purchaseLocalDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (purchaseLocalDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Last Purchase Date cannot be in the future.");
            }
            LocalDate lastPurchaseDate = purchaseLocalDate;

            Customer customer = new Customer(id, name, age, membershipLevel, lastPurchaseDate);

            if (idField.getText().isEmpty()) {
                managementService.saveCustomer(customer);
                JOptionPane.showMessageDialog(this, "Customer created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                managementService.updateCustomer(customer);
                JOptionPane.showMessageDialog(this, "Customer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            clearForm();
            loadCustomerData();
            mainFrame.refreshMetricsDashboard();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for Age.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteAction(ActionEvent e) {
        String idToDelete = idField.getText();
        if (idToDelete.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete customer ID: " + idToDelete + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = managementService.deleteCustomer(idToDelete);
            if (success) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadCustomerData();
                mainFrame.refreshMetricsDashboard();

            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    // EXPORT ACTION HANDLER (Unchanged)
    // =========================================================

    private void handleExportAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Customer Data to CSV");

        fileChooser.setSelectedFile(new File("customers_export.csv"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            try {
                managementService.exportCustomersToCsv(filePath);
                mainFrame.updateStatusBar("Customer data successfully exported to: " + filePath, true);

                JOptionPane.showMessageDialog(this,
                        "Customer data successfully exported to:\n" + filePath,
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error writing file: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Export failed: " + ex.getMessage());
            }
        }
    }
    // =========================================================
// RBAC Implementation (Fine-Grained Button Control)
// =========================================================

    /**
     * Applies fine-grained control to the CRUD buttons based on the active user's role.
     * Roles with CRUD access: Admin, Manager, Customer Service Agent
     * Roles with Read-Only access: HR Specialist, Data Analyst
     */
    public void refreshAccessControls() {
        // Check if a user is logged in before attempting to get the role
        if (managementService.getAuthService().getActiveUser() == null) {
            setCrudEnabled(false);
            exportButton.setEnabled(false);
            return;
        }

        // Retrieve the active user's role from the Authentication Service
        String role = managementService.getAuthService().getActiveUser().getRole();

        // Define which roles have CRUD privileges on CUSTOMER data
        boolean hasCrudAccess = role.equals("Admin") ||
                role.equals("Manager") ||
                role.equals("Customer Service Agent");

        // Roles that only have Read access on this panel
        boolean isReadOnly = role.equals("HR Specialist") || role.equals("Data Analyst");

        // Set state for the CRUD buttons and input fields
        if (hasCrudAccess) {
            setCrudEnabled(true);
            mainFrame.updateStatusBar("Customer Panel: CRUD mode enabled for " + role, true);
        } else if (isReadOnly) {
            setCrudEnabled(false);
            mainFrame.updateStatusBar("Customer Panel: Read-Only mode enabled for " + role, true);
        } else {
            // Should be blocked at the tab level, but set to Read-Only as a fallback.
            setCrudEnabled(false);
        }

        // Data Analyst is the only Read-Only role allowed to Export. Manager and Admin also allowed.
        boolean canExport = role.equals("Admin") || role.equals("Manager") || role.equals("Data Analyst");
        exportButton.setEnabled(canExport);
    }

    /**
     * Helper method to enable or disable CRUD buttons and input fields.
     */
    private void setCrudEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
        deleteButton.setEnabled(false); // Always disable delete initially until selection is made

        // Disable editing in the form fields
        nameField.setEditable(enabled);
        ageField.setEditable(enabled);
        membershipLevelComboBox.setEnabled(enabled);
        lastPurchaseDateChooser.setEnabled(enabled);

        // Make the table non-editable for Read-Only roles (or at least appearance)
        customerTable.setEnabled(enabled);
    }
}