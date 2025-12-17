package service;

import dao.UserDAO;
import dao.sqlite.UserDAOImpl;
import model.User;
import util.CustomExceptions;
import util.PasswordHasher;
import java.util.List;
import java.security.Security;

public class AuthenticationService {

    private final UserDAO userDAO;
    private User activeUser = null; // Holds the currently logged-in user

    /**
     * Constructor initializes the DAO dependency.
     */
    public AuthenticationService() {
        // Direct instantiation is acceptable in this simple architecture.
        // NOTE: UserDAOImpl must implement UserDAO for this to be valid.
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Attempts to log in a user with the provided credentials.
     * @param username The username entered by the user.
     * @param password The password entered by the user (plain text).
     * @return The authenticated User object.
     * @throws CustomExceptions.RecordNotFoundException if user is not found in the database.
     * @throws SecurityException if the password is incorrect.
     */
    public User login(String username, String password) throws CustomExceptions.RecordNotFoundException, SecurityException {
        // 1. Look up the user by username to get the stored hash and role
        // This relies on the fixed UserDAOImpl.findByUsername()
        User user = userDAO.findByUsername(username);

        // 2. Verify the plain text password against the stored hash
        if (PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
            this.activeUser = user; // Set the active session
            System.out.println("User " + username + " successfully logged in with role: " + user.getRole());
            return user;
        } else {
            // Password verification failed
            throw new SecurityException("Invalid password for user: " + username);
        }
    }

    /**
     * Logs out the current user by clearing the active session.
     */
    public void logout() {
        if (this.activeUser != null) {
            System.out.println("User " + this.activeUser.getUsername() + " logged out.");
        }
        this.activeUser = null;
    }

    // --- FIX: ADDED METHOD TO ALLOW MAIN FRAME TO SET THE ACTIVE USER ---
    /**
     * Explicitly sets the active user session.
     * Used primarily by MainFrame after a successful login handled by the LoginFrame.
     * @param user The authenticated User object.
     */
    public void setActiveUser(User user) {
        this.activeUser = user;
    }
    // ----------------------------------------------------------------------

    /**
     * Gets the currently logged-in user.
     * @return The active User object, or null if no one is logged in.
     */
    public User getActiveUser() {
        return activeUser;
    }

    /**
     * Registers a new user account by performing validation and password hashing.
     * * @param username The desired username (must be unique).
     * @param plainPassword The plain text password from the UI.
     * @param role The role assigned to the user (e.g., "Manager", "Admin").
     * @return The newly created User object.
     * @throws IllegalArgumentException if the username is already taken or inputs are invalid.
     */
    public User registerNewUser(String username, String plainPassword, String role) throws IllegalArgumentException {
        // --- 1. Basic Input Validation ---
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("User role must be specified.");
        }

        // --- 2. Uniqueness Check (Delegates to DAO) ---
        // Requires the new public method we added to UserDAOImpl
        if (((UserDAOImpl) userDAO).checkIfUsernameExists(username.trim())) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken. Please choose another.");
        }

        // --- 3. Security: Hash the Password ---
        try {
            String hashedPassword = PasswordHasher.hashPassword(plainPassword);

            // --- 4. Create and Save the User Model ---
            User newUser = new User(username.trim(), hashedPassword, role.trim());

            // The save method handles the insertion
            userDAO.save(newUser);

            System.out.println("New user registered: " + username + " with role " + role);
            return newUser;

        } catch (Exception e) {
            // Catch errors from PasswordHasher or DAO saving
            throw new IllegalArgumentException("Error saving new user: " + e.getMessage());
        }
    }

    /**
     * Retrieves all users for administrative display.
     * @return List of all User objects.
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Deletes a user account by username.
     * @param username The username of the user to delete.
     * @return true if deletion was successful.
     */
    public boolean deleteUser(String username) {
        return userDAO.delete(username);
    }

    // --- Utility for Initial Setup (Seeding an Admin User) ---

    /**
     * Used one time to create the first admin user if the database is empty.
     * This ensures the application is usable immediately after setup.
     * @param username The admin username.
     * @param plainPassword The plain text password.
     * @param role The role (e.g., "Admin").
     */
    public void setupInitialAdmin(String username, String plainPassword, String role) {
        try {
            // Try to find the user; if found, do nothing
            userDAO.findByUsername(username);
            System.out.println("Admin user '" + username + "' already exists. Skipping setup.");
        } catch (CustomExceptions.RecordNotFoundException e) {
            // User not found, proceed to create
            System.out.println("No admin found. Creating initial admin user...");
            try {
                String hashedPassword = PasswordHasher.hashPassword(plainPassword);
                User newAdmin = new User(username, hashedPassword, role);

                // This calls the fixed UserDAOImpl.save() method
                userDAO.save(newAdmin);
                System.out.println("Initial Admin user created: " + username);
            } catch (Exception saveEx) {
                System.err.println("FATAL: Failed to save initial admin user. Check UserDAOImpl.save() and database connection: " + saveEx.getMessage());
                // This failure is critical but might be masked during startup
            }
        }
    }
}