package model;

// Represents a system user, not a domain entity like Employee or Customer.
public class User {
    private String username;
    private String hashedPassword;
    private String role; // e.g., "Admin", "Manager"

    // Constructor
    public User(String username, String hashedPassword, String role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    // Getters
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getRole() { return role; }

    // Setters (useful for password/role updates)
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}