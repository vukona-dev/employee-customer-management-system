package gui;

import model.User;
import service.ManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel dedicated to administrative user account management (CRUD for Users).
 * This panel should typically only be accessible to 'Admin' roles.
 */
public class UserManagementPanel extends JPanel {

    private final ManagementService managementService;
    private final MainFrame mainFrame;

    // --- GUI Components ---
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    private JButton deleteButton;
    private JTable userTable;
    private DefaultTableModel tableModel;

    // --- Dropdown Data for Roles ---
    private static final String[] ROLES = {
            "Manager",
            "HR Specialist",        // Handles Employee CRUD
            "Customer Service Agent", // Handles Customer CRUD
            "Data Analyst",         // Handles Reporting/View Only
            "Admin"                 // Keep this role last
    };

    public UserManagementPanel(ManagementService service, MainFrame mainFrame) {
        this.managementService = service;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadUserTable();
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

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Register New System User"));

        // A. Input Fields Panel (GridLayout for labels and fields)
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        usernameField = new JTextField(20);
        fieldsPanel.add(new JLabel("Username:"));
        fieldsPanel.add(usernameField);

        passwordField = new JPasswordField(20);
        fieldsPanel.add(new JLabel("Password:"));
        fieldsPanel.add(passwordField);

        confirmPasswordField = new JPasswordField(20);
        fieldsPanel.add(new JLabel("Confirm Password:"));
        fieldsPanel.add(confirmPasswordField);

        roleComboBox = new JComboBox<>(ROLES);
        roleComboBox.setSelectedItem("Manager");
        fieldsPanel.add(new JLabel("Role:"));
        fieldsPanel.add(roleComboBox);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // B. Control Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        registerButton = new JButton("Register User");
        deleteButton = new JButton("Delete Selected User");
        JButton clearButton = new JButton("Clear Form");

        buttonPanel.add(clearButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Setup initial state
        clearButton.addActionListener(e -> clearForm());
        deleteButton.setEnabled(false); // Disable delete until a row is selected

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Username", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Users should not be edited via the table cell
            }
        };

        userTable = new JTable(tableModel);

        // Listener to populate the form when a row is clicked (for Delete action)
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) {
                // Populate the username field to confirm deletion target
                int rowIndex = userTable.getSelectedRow();
                String selectedUsername = tableModel.getValueAt(rowIndex, 0).toString();
                usernameField.setText(selectedUsername);

                // Clear password fields for security
                passwordField.setText("");
                confirmPasswordField.setText("");

                // Set the delete button context
                deleteButton.setEnabled(true);
                mainFrame.updateStatusBar("User selected: " + selectedUsername, true);
            }
        });

        return new JScrollPane(userTable);
    }

    // =========================================================
    // Data Loading and Mapping
    // =========================================================

    public void loadUserTable() {
        tableModel.setRowCount(0);

        try {
            List<User> users = managementService.getAllUsers();

            for (User user : users) {
                // IMPORTANT: Never display the hashed password!
                tableModel.addRow(new Object[]{
                        user.getUsername(),
                        user.getRole()
                });
            }
            mainFrame.updateStatusBar("User accounts loaded successfully.", true);

        } catch (Exception ex) {
            mainFrame.updateStatusBar("Error loading user accounts: " + ex.getMessage(), false);
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        roleComboBox.setSelectedItem("Manager");
        deleteButton.setEnabled(false);
        userTable.clearSelection();
        mainFrame.resetStatusBar();
    }

    // =========================================================
    // Action Handlers
    // =========================================================

    private void setupActions() {
        registerButton.addActionListener(this::handleRegisterAction);
        deleteButton.addActionListener(this::handleDeleteAction);
    }

    private void handleRegisterAction(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        try {
            // 1. Local UI Validation
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                throw new IllegalArgumentException("All fields must be filled.");
            }
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Password and Confirm Password do not match.");
            }

            // 2. Service Layer Registration (Handles Uniqueness, Hashing, and Saving)
            managementService.registerNewUser(username, password, role);

            mainFrame.updateStatusBar("User '" + username + "' registered successfully!", true);
            clearForm();
            loadUserTable(); // Refresh the table

        } catch (IllegalArgumentException ex) {
            // Catches validation errors from this panel AND the service layer (e.g., username taken)
            mainFrame.updateStatusBar("Registration Failed: " + ex.getMessage(), false);
        } catch (Exception ex) {
            mainFrame.updateStatusBar("System Error during registration: " + ex.getMessage(), false);
        }
    }

    private void handleDeleteAction(ActionEvent e) {
        String usernameToDelete = usernameField.getText();

        if (usernameToDelete.isEmpty() || userTable.getSelectedRow() == -1) {
            mainFrame.updateStatusBar("Please select a user from the table to delete.", false);
            return;
        }

        // Prevent deletion of the currently logged-in user (Critical Security Check)
        if (managementService.getAuthService().getActiveUser() != null &&
                managementService.getAuthService().getActiveUser().getUsername().equals(usernameToDelete)) {
            mainFrame.updateStatusBar("Error: Cannot delete the currently logged-in user.", false);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user: " + usernameToDelete + "?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (managementService.deleteUser(usernameToDelete)) {
                    mainFrame.updateStatusBar("User '" + usernameToDelete + "' deleted successfully.", true);
                    clearForm();
                    loadUserTable(); // Refresh the table
                } else {
                    mainFrame.updateStatusBar("Error: Failed to delete user. User not found or data access issue.", false);
                }
            } catch (Exception ex) {
                mainFrame.updateStatusBar("System Error during deletion: " + ex.getMessage(), false);
            }
        }
    }
}