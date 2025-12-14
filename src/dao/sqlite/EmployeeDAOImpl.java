package dao.sqlite;

import dao.GenericDAO;
import model.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of the GenericDAO interface for the Employee entity.
 * Handles all database operations (CRUD) for Employees using SQLite.
 */
public class EmployeeDAOImpl implements GenericDAO<Employee> {

    private Connection connection;

    public EmployeeDAOImpl() {
        // Get the single active connection instance
        this.connection = SQLiteConnection.getInstance().getConnection();
    }

    // --- CREATE Operation (Save) ---
    @Override
    public void save(Employee employee) {
        String sql = "INSERT INTO Employees (id, name, age, salary, jobTitle) VALUES (?, ?, ?, ?, ?)";

        // Use try-with-resources to ensure PreparedStatement is closed
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, employee.getId());
            pstmt.setString(2, employee.getName());
            pstmt.setInt(3, employee.getAge());
            pstmt.setDouble(4, employee.getSalary());
            pstmt.setString(5, employee.getJobTitle());

            pstmt.executeUpdate();
            System.out.println("Employee saved successfully: " + employee.getName());

        } catch (SQLException e) {
            System.err.println("Error saving employee to DB: " + e.getMessage());
            // You should throw a checked or unchecked exception here for proper handling
        }
    }

    // --- READ Operation (Find by ID) ---
    @Override
    public Employee findById(String id) {
        String sql = "SELECT * FROM Employees WHERE id = ?";
        Employee employee = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Call a helper method to map the ResultSet row to an Employee object
                    employee = mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding employee by ID: " + e.getMessage());
        }
        return employee;
    }

    // --- READ Operation (Find All) ---
    @Override
    public List<Employee> findAll() {
        String sql = "SELECT * FROM Employees";
        List<Employee> employees = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all employees: " + e.getMessage());
        }
        return employees;
    }

    // --- UPDATE Operation ---
    @Override
    public void update(Employee employee) {
        String sql = "UPDATE Employees SET name = ?, age = ?, salary = ?, jobTitle = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getAge());
            pstmt.setDouble(3, employee.getSalary());
            pstmt.setString(4, employee.getJobTitle());
            pstmt.setString(5, employee.getId()); // ID is used in the WHERE clause

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Warning: Employee ID not found for update: " + employee.getId());
            } else {
                System.out.println("Employee updated successfully: " + employee.getName());
            }

        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
        }
    }

    // --- DELETE Operation ---
    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Employees WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Returns true if one or more rows were deleted

        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to convert a database row (ResultSet) into a Java Employee object.
     * @param rs The ResultSet object.
     * @return A fully populated Employee object.
     * @throws SQLException If a column access error occurs.
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getDouble("salary"),
                rs.getString("jobTitle")
        );
    }
}