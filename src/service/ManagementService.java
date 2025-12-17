package service;

import dao.GenericDAO;
import dao.sqlite.CustomerDAOImpl;
import dao.sqlite.EmployeeDAOImpl;
import model.Customer;
import model.Employee;
import model.Person;
import util.DataExporter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;


/**
 * The core business logic service for the ECMS.
 * Acts as the 'Controller' layer, coordinating data between the GUI and DAOs.
 * This is where authentication, validation, analytics, and file exports are handled.
 */
public class ManagementService {

    // --- Core Dependencies ---
    private final EmployeeDAOImpl employeeDAO;
    private final CustomerDAOImpl customerDAO;

    // --- Authentication Service for Login/Active User access ---
    private final AuthenticationService authService;

    // --- Analytics Engine (Used for calculations) ---
    private final AnalyticsEngine analyticsEngine;

    // --- Constructor ---
    public ManagementService() {
        // Initialize the concrete DAO implementations
        this.employeeDAO = new EmployeeDAOImpl();
        this.customerDAO = new CustomerDAOImpl();

        // Initialize CORE SERVICES
        this.authService = new AuthenticationService();
        this.analyticsEngine = new AnalyticsEngine();
    }

    // =========================================================
    // SERVICE ACCESSORS
    // =========================================================

    public AuthenticationService getAuthService() {
        return authService;
    }

    public AnalyticsEngine getAnalyticsEngine() {
        return analyticsEngine;
    }

    // =========================================================
    // CRUD OPERATIONS (Employee)
    // =========================================================

    public void saveEmployee(Employee employee) throws IllegalArgumentException {
        if (!validateEmployee(employee)) {
            throw new IllegalArgumentException("Employee validation failed. Name and Job Title must not be empty, Age must be > 18.");
        }
        employeeDAO.save(employee);
    }

    public Optional<Employee> getEmployeeById(String id) {
        return Optional.ofNullable(employeeDAO.findById(id));
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }

    public void updateEmployee(Employee employee) throws IllegalArgumentException {
        if (!validateEmployee(employee)) {
            throw new IllegalArgumentException("Employee validation failed during update.");
        }
        employeeDAO.update(employee);
    }

    public boolean deleteEmployee(String id) {
        return employeeDAO.delete(id);
    }

    // =========================================================
    // CRUD OPERATIONS (Customer)
    // =========================================================

    public void saveCustomer(Customer customer) throws IllegalArgumentException {
        // NOTE: Adjusted to handle the date picker change, but kept your original validation style
        if (!validateCustomer(customer) || customer.getLastPurchaseDate() == null) {
            throw new IllegalArgumentException("Customer validation failed. Name, Membership Level, and Last Purchase Date must be valid.");
        }
        customerDAO.save(customer);
    }

    public Optional<Customer> getCustomerById(String id) {
        return Optional.ofNullable(customerDAO.findById(id));
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public void updateCustomer(Customer customer) throws IllegalArgumentException {
        // NOTE: Adjusted to handle the date picker change, but kept your original validation style
        if (!validateCustomer(customer) || customer.getLastPurchaseDate() == null) {
            throw new IllegalArgumentException("Customer validation failed during update.");
        }
        customerDAO.update(customer);
    }

    public boolean deleteCustomer(String id) {
        return customerDAO.delete(id);
    }


    // =========================================================
// USER ACCOUNT MANAGEMENT (Delegate Calls to AuthService)
// =========================================================

    /**
     * Registers a new user account, delegating validation and hashing to the AuthService.
     * @param username The desired username (must be unique).
     * @param plainPassword The plain text password from the UI.
     * @param role The role assigned to the user.
     * @throws IllegalArgumentException if the username is already taken or inputs are invalid.
     */
    public void registerNewUser(String username, String plainPassword, String role) throws IllegalArgumentException {
        // The AuthService handles the security (hashing) and uniqueness check.
        authService.registerNewUser(username, plainPassword, role);
    }

    /**
     * Retrieves all user accounts for administrative display.
     * @return List of all User objects.
     */
    public List<model.User> getAllUsers() {
        return authService.getAllUsers();
    }

    /**
     * Deletes a user account by username.
     * @param username The username of the user to delete.
     * @return true if deletion was successful.
     */
    public boolean deleteUser(String username) {
        return authService.deleteUser(username);
    }

    // =========================================================
    // VALIDATION LOGIC (Business Rules)
    // =========================================================

    private boolean validateEmployee(Employee e) {
        if (e.getName() == null || e.getName().trim().isEmpty() ||
                e.getJobTitle() == null || e.getJobTitle().trim().isEmpty()) {
            return false;
        }
        if (e.getAge() < 18) {
            return false;
        }
        return true;
    }

    private boolean validateCustomer(Customer c) {
        if (c.getName() == null || c.getName().trim().isEmpty() ||
                c.getMembershipLevel() == null || c.getMembershipLevel().trim().isEmpty()) {
            return false;
        }
        if (c.getAge() < 18) {
            return false;
        }
        return true;
    }

    // =========================================================
    // DASHBOARD & ANALYTICS INTEGRATION (Delegate Calls to Engine)
    // =========================================================

    // Dashboard Metrics
    public double getAverageAge() {
        List<Person> people = (List<Person>) (List<?>) getAllEmployees();
        people.addAll(getAllCustomers());
        return analyticsEngine.calculateAverageAge(people);
    }

    public double getAverageSalary() {
        return analyticsEngine.calculateAverageSalary(getAllEmployees());
    }

    // This method is likely for the dashboard/basic count, not the deep analytics map
    public long getMembershipCount(String level) {
        return analyticsEngine.countMembershipLevel(getAllCustomers(), level);
    }

    // --- NEW ANALYTICS METHODS (Required by AnalyticsPanel) ---

    /**
     * Delegates calculation of average salary per job title to the AnalyticsEngine.
     * @return Map of job title to average salary.
     */
    public Map<String, Double> getAverageSalaryByJobTitle() {
        return analyticsEngine.getAverageSalaryByJobTitle(getAllEmployees());
    }

    /**
     * Delegates calculation of employee count per job title to the AnalyticsEngine.
     * @return Map of job title to employee count.
     */
    public Map<String, Integer> getEmployeeCountByJobTitle() {
        return analyticsEngine.getEmployeeCountByJobTitle(getAllEmployees());
    }

    /**
     * Delegates calculation of customer count per membership level to the AnalyticsEngine.
     * @return Map of membership level to customer count.
     */
    public Map<String, Integer> getCustomerCountByMembershipLevel() {
        return analyticsEngine.getCustomerCountByMembershipLevel(getAllCustomers());
    }

    // =========================================================
    // CSV FILE EXPORT OPERATIONS
    // =========================================================

    public void exportEmployeesToCsv(String filePath) throws IOException {
        List<Employee> employees = getAllEmployees();
        String[] header = {"ID", "Name", "Job Title", "Age", "Salary"};

        List<String[]> dataRows = employees.stream()
                .map(e -> new String[]{
                        e.getId(),
                        e.getName(),
                        e.getJobTitle(),
                        String.valueOf(e.getAge()),
                        String.format("%.2f", e.getSalary())
                })
                .collect(Collectors.toList());

        DataExporter.exportToCsv(filePath, header, dataRows);
    }

    public void exportCustomersToCsv(String filePath) throws IOException {
        List<Customer> customers = getAllCustomers();
        String[] header = {"ID", "Name", "Age", "Membership Level", "Last Purchase Date"};

        List<String[]> dataRows = customers.stream()
                .map(c -> new String[]{
                        c.getId(),
                        c.getName(),
                        String.valueOf(c.getAge()),
                        c.getMembershipLevel(),
                        c.getLastPurchaseDate() != null ? c.getLastPurchaseDate().toString() : ""
                })
                .collect(Collectors.toList());

        DataExporter.exportToCsv(filePath, header, dataRows);
    }
}