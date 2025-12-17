package dao.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the singleton connection to the SQLite database.
 * The connection is established once and held open for the application lifetime.
 */
public class SQLiteConnection {

    // --- Singleton Instance ---
    private static SQLiteConnection instance = null;

    // --- Database File Path ---
    // Simplified path: file will be created in the project root directory
    private static final String DB_URL = "jdbc:sqlite:ecms_db.sqlite";

    // --- Connection Object ---
    // CRITICAL: This instance variable holds the active connection.
    private Connection connection;

    /**
     * Private constructor to prevent direct instantiation (enforcing Singleton).
     * Establishes the connection and calls initializeTables.
     */
    private SQLiteConnection() {
        try {
            // 1. Load the SQLite JDBC Driver class
            Class.forName("org.sqlite.JDBC");

            // 2. Establish and RETAIN the connection.
            // DO NOT use try-with-resources here, or it will auto-close the connection.
            this.connection = DriverManager.getConnection(DB_URL);

            // 3. Ensure tables exist immediately after connection
            initializeTables();

        } catch (ClassNotFoundException e) {
            System.err.println("Error: SQLite JDBC Driver not found.");
            throw new RuntimeException("SQLite JDBC Driver missing.", e);
        } catch (SQLException e) {
            System.err.println("Error connecting to the database at: " + DB_URL);
            throw new RuntimeException("Failed to establish database connection.", e);
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
     * @return The persistent Connection object.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Creates the Employee, Customer, and User tables if they do not already exist.
     * Uses the retained connection to create a Statement (which is closed properly).
     */
    private void initializeTables() {
        if (connection == null) return; // Safety check

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
                        "lastPurchaseDate TEXT NOT NULL" +
                        ");";

        // SQL for creating the User table for authentication
        String createUserTable =
                "CREATE TABLE IF NOT EXISTS Users (" +
                        "username TEXT PRIMARY KEY," +
                        "hashed_password TEXT NOT NULL," +
                        "role TEXT NOT NULL" +
                        ");";

        // Use try-with-resources on the Statement to close it, while keeping 'connection' open.
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(createEmployeeTable);
            statement.execute(createCustomerTable);
            statement.execute(createUserTable);
            System.out.println("Database tables checked/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating database tables: " + e.getMessage());
            // This is a critical failure, we should not proceed if tables can't be created.
            throw new RuntimeException("Failed to initialize database tables.", e);
        }
    }
}