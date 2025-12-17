package gui;

import dao.sqlite.SQLiteConnection; // <--- NEW IMPORT
import service.ManagementService;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * The official entry point of the ECMS application.
 * Now manages the start-up sequence to enforce user authentication.
 */
public class Main {
    public static void main(String[] args) {

        // --- CRITICAL STEP: FORCE DATABASE INITIALIZATION ---
        // This ensures the Singleton connection is established, the ecms_db.sqlite file
        // is created, and all tables (including 'Users') are initialized immediately.
        try {
            SQLiteConnection.getInstance();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "FATAL ERROR: Could not establish database connection or create tables: " + e.getMessage(),
                    "Database Startup Failure", JOptionPane.ERROR_MESSAGE);
            return; // Stop application launch if database fails
        }

        // Swing applications must start on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Initialize the Service layer (now that the DB connection is confirmed)
                ManagementService service = new ManagementService();

                // 2. Initialize the MainFrame
                MainFrame mainFrame = new MainFrame(service);

                // 3. Start the application by displaying the Login window
                new LoginFrame(mainFrame).setVisible(true);

            } catch (Exception e) {
                // Catch any fatal errors during initialization (e.g., Service/DAO creation failure)
                JOptionPane.showMessageDialog(null,
                        "Fatal application error during UI startup: " + e.getMessage(),
                        "Initialization Failed", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}