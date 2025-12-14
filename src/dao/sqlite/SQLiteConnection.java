package dao.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the singleton connection to the SQLite database.
 * Also handles the initial setup (creation) of necessary tables.
 */
public class SQLiteConnection {

    // --- Singleton Instance ---
    private static SQLiteConnection instance = null;

    // --- Database File Path ---
    // The path is relative to the project root where the 'resources' folder is located.
    private static final String DB_URL = "jdbc:sqlite:resources/ecms.db";

    // --- Connection Object ---
    private Connection connection;

    /**
     * Private constructor to prevent direct instantiation (enforcing Singleton).
     */
    private SQLiteConnection() {
        try {
            // 1. Load the SQLite JDBC Driver class
            Class.forName("org.sqlite.JDBC");

            // 2. Establish the connection
            this.connection = DriverManager.getConnection(DB_URL);

            // 3. Ensure tables exist immediately after connection
            initializeTables();

        } catch (ClassNotFoundException e) {
            System.err.println("Error: SQLite JDBC Driver not found.");
            // In a real app, you would throw a runtime exception here
        } catch (SQLException e) {
            System.err.println("Error connecting to the database at: " + DB_URL);
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Public static method to get the single instance of the class (Singleton access).
     * @return The single SQLiteConnection instance.
     */
    public static SQLiteConnection getInstance() {
        if (instance == null) {
            instance = new SQLiteConnection();
        }
        return instance;
    }

    /**
     * Gets the active database connection object.
     * @return The Connection object.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Creates the Employee and Customer tables if they do not already exist.
     */
    private void initializeTables() {
        if (connection == null) return; // Should not happen, but a safe check

        // SQL for creating the Employee table
        String createEmployeeTable =
                "CREATE TABLE IF NOT EXISTS Employees (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "age INTEGER NOT NULL," +
                        "salary REAL NOT NULL," +
                        "jobTitle TEXT NOT NULL" +
                        ");";

        // SQL for creating the Customer table
        String createCustomerTable =
                "CREATE TABLE IF NOT EXISTS Customers (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "age INTEGER NOT NULL," +
                        "membershipLevel TEXT NOT NULL," +
                        "lastPurchaseDate TEXT NOT NULL" + // Store LocalDate as TEXT (ISO-8601 format)
                        ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createEmployeeTable);
            statement.execute(createCustomerTable);
            System.out.println("Database tables checked/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating database tables: " + e.getMessage());
        }
    }
}