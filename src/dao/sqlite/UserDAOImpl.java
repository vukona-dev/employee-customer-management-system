package dao.sqlite;

import dao.GenericDAO;
import dao.UserDAO; // CRITICAL: Now implementing both interfaces
import model.User;
import util.CustomExceptions.RecordNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// CRITICAL FIX: Implements BOTH interfaces (GenericDAO and UserDAO) to resolve the 'Incompatible types' error.
public class UserDAOImpl implements GenericDAO<User>, UserDAO {

    /**
     * Saves a new User record into the database. Essential for initial admin setup.
     * FIX: The Connection is retrieved outside the try-with-resources block.
     */
    @Override
    public void save(User user) {
        String sql = "INSERT OR IGNORE INTO Users (username, hashed_password, role) VALUES (?, ?, ?)";

        // 1. Get the persistent connection outside of try-with-resources
        Connection conn = SQLiteConnection.getInstance().getConnection();

        // 2. Use try-with-resources ONLY for the PreparedStatement
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getRole());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            // This is where the "database connection closed" error was occurring
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    /**
     * Finds a user by their username. This is the primary method for authentication.
     * FIX: The Connection is retrieved outside the try-with-resources block.
     * @param username The username to search for.
     * @return The User object if found.
     * @throws RecordNotFoundException if no user with that username exists.
     */
    @Override
    public User findByUsername(String username) throws RecordNotFoundException {
        String sql = "SELECT username, hashed_password, role FROM Users WHERE username = ?";

        // 1. Get the persistent connection outside of try-with-resources
        Connection conn = SQLiteConnection.getInstance().getConnection();

        // 2. Use try-with-resources ONLY for the PreparedStatement (ResultSet is closed by the Statement)
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // User found, map the data
                String storedUsername = rs.getString("username");
                String hashedPassword = rs.getString("hashed_password");
                String role = rs.getString("role");

                // Assuming your User model constructor takes (username, hashedPassword, role)
                return new User(storedUsername, hashedPassword, role);
            } else {
                // User NOT found
                throw new RecordNotFoundException("User not found with username: " + username);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error finding user by username: " + e.getMessage());
            // Treat SQL error as "not found" to prevent security leaks
            throw new RecordNotFoundException("Database error during user search.", e);
        }
    }

    /**
     * Checks if a user with the given username already exists in the database.
     * Used by the service layer during new user registration to enforce uniqueness.
     * @param username The username to check.
     * @return true if a user with that username exists, false otherwise.
     */
    public boolean checkIfUsernameExists(String username) {
        // Since findByUsername handles the core logic, we can reuse it
        try {
            findByUsername(username);
            return true; // If findByUsername returns successfully, the user exists
        } catch (RecordNotFoundException e) {
            return false; // If RecordNotFoundException is thrown, the user does not exist
        } catch (Exception e) {
            System.err.println("Database error during username check: " + e.getMessage());
            return true; // On any other DB error, assume it exists to prevent creation (fail safe)
        }
    }

    // =========================================================
    // GENERIC DAO IMPLEMENTATIONS (Required by GenericDAO interface)
    // =========================================================

    /**
     * Implementation required by GenericDAO. Since username is the PK, we delegate.
     * @param id The ID (username) to search for.
     * @return The User object if found.
     */
    @Override
    public User findById(String id) throws RecordNotFoundException {
        // Delegate to the functional findByUsername method
        return findByUsername(id);
    }

    /**
     * Retrieves all User records from the database.
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        // Note: We deliberately exclude the hashed_password from the SELECT query
        // in a real app, but for simplicity here, we select everything.
        String sql = "SELECT username, hashed_password, role FROM Users";

        Connection conn = SQLiteConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) { // Execute query with the Statement

            while (rs.next()) {
                // Map the data to a User object
                String storedUsername = rs.getString("username");
                String hashedPassword = rs.getString("hashed_password");
                String role = rs.getString("role");

                users.add(new User(storedUsername, hashedPassword, role));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error retrieving all users: " + e.getMessage());
        }
        return users;
    }
    /**
     * Implementation required by GenericDAO. Basic stub for now.
     */
    @Override
    public void update(User entity) {
        // Not required for basic login, do nothing
        System.out.println("User update called (Stub)");
    }

    /**
     * Deletes a user record based on the username.
     * @param username The username of the user to delete.
     * @return true if one record was deleted, false otherwise.
     */
    @Override
    public boolean delete(String username) {
        String sql = "DELETE FROM Users WHERE username = ?";

        Connection conn = SQLiteConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0; // Returns true if one or more rows were deleted

        } catch (SQLException e) {
            System.err.println("SQL Error deleting user: " + e.getMessage());
            return false;
        }
    }
}