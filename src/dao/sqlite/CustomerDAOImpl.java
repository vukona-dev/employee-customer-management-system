package dao.sqlite;

import dao.GenericDAO;
import model.Customer;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of the GenericDAO interface for the Customer entity.
 * Handles all database operations (CRUD) for Customers using SQLite.
 */
public class CustomerDAOImpl implements GenericDAO<Customer> {

    private Connection connection;

    public CustomerDAOImpl() {
        // Get the single active connection instance
        this.connection = SQLiteConnection.getInstance().getConnection();
    }

    // --- CREATE Operation (Save) ---
    @Override
    public void save(Customer customer) {
        String sql = "INSERT INTO Customers (id, name, age, membershipLevel, lastPurchaseDate) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getName());
            pstmt.setInt(3, customer.getAge());
            pstmt.setString(4, customer.getMembershipLevel());

            // Convert LocalDate to String (ISO-8601 format) for storage in TEXT column
            pstmt.setString(5, customer.getLastPurchaseDate().toString());

            pstmt.executeUpdate();
            System.out.println("Customer saved successfully: " + customer.getName());

        } catch (SQLException e) {
            System.err.println("Error saving customer to DB: " + e.getMessage());
        }
    }

    // --- READ Operation (Find by ID) ---
    @Override
    public Customer findById(String id) {
        String sql = "SELECT * FROM Customers WHERE id = ?";
        Customer customer = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    customer = mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding customer by ID: " + e.getMessage());
        }
        return customer;
    }

    // --- READ Operation (Find All) ---
    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM Customers";
        List<Customer> customers = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all customers: " + e.getMessage());
        }
        return customers;
    }

    // --- UPDATE Operation ---
    @Override
    public void update(Customer customer) {
        String sql = "UPDATE Customers SET name = ?, age = ?, membershipLevel = ?, lastPurchaseDate = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setInt(2, customer.getAge());
            pstmt.setString(3, customer.getMembershipLevel());
            pstmt.setString(4, customer.getLastPurchaseDate().toString());
            pstmt.setString(5, customer.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Warning: Customer ID not found for update: " + customer.getId());
            } else {
                System.out.println("Customer updated successfully: " + customer.getName());
            }

        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
        }
    }

    // --- DELETE Operation ---
    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Customers WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to convert a database row (ResultSet) into a Java Customer object.
     * @param rs The ResultSet object.
     * @return A fully populated Customer object.
     * @throws SQLException If a column access error occurs.
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        // Convert the TEXT (ISO-8601 string) back to a LocalDate object
        LocalDate purchaseDate = LocalDate.parse(rs.getString("lastPurchaseDate"));

        return new Customer(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("membershipLevel"),
                purchaseDate
        );
    }
}