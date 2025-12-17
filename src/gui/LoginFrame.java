package gui;

import service.AuthenticationService;
import util.CustomExceptions;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

// Assuming your MainFrame is also in the 'gui' package
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final MainFrame mainFrame; // Reference to the main application window

    public LoginFrame(MainFrame mainFrame) {
        // Initialize the service and the main frame reference
        this.authService = new AuthenticationService();
        this.mainFrame = mainFrame;

        setTitle("Login - ECMS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Initialize Components ---
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");

        // --- Create Panel for Form ---
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        // Placeholder for an empty label to space out the grid
        formPanel.add(new JLabel());
        formPanel.add(loginButton);

        add(formPanel, BorderLayout.CENTER);

        // --- Action Listener ---
        loginButton.addActionListener(this::handleLogin);

        // --- Initial Admin Setup (CRUCIAL RUN-ONCE STEP) ---
        // Creates the 'admin' user if it doesn't exist.
        // Credentials: username="admin", password="password123", role="Admin"
        authService.setupInitialAdmin("admin", "password123", "Admin");


        pack();
        setLocationRelativeTo(null); // Center the window
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User loggedInUser = authService.login(username, password);

            // Success:
            JOptionPane.showMessageDialog(this, "Welcome, " + loggedInUser.getUsername() + "! You are logged in as " + loggedInUser.getRole() + ".", "Login Success", JOptionPane.INFORMATION_MESSAGE);

            // 1. Tell the MainFrame who is logged in (for RBAC)
            mainFrame.setActiveUser(loggedInUser);
            // 2. Show the main application window
            mainFrame.setVisible(true);
            // 3. Close the login window
            this.dispose();

        } catch (CustomExceptions.RecordNotFoundException | SecurityException ex) {
            // Handles both "user not found" and "password incorrect" with a generic message
            JOptionPane.showMessageDialog(this, "Login Failed: Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Catch unexpected errors
            JOptionPane.showMessageDialog(this, "An unexpected system error occurred: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}