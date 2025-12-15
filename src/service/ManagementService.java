package service;

import dao.GenericDAO;
import dao.file.JsonExporter;
import dao.file.TextExporter;
import dao.sqlite.CustomerDAOImpl;
import dao.sqlite.EmployeeDAOImpl;
import model.Customer;
import model.Employee;
import model.Person;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The core business logic service for the ECMS.
 * Acts as the 'Controller' layer, coordinating data between the GUI and DAOs.
 * This is where validation, analytics requests, and file exports are handled.
 */
public class ManagementService {

    // DAO (Data Access Objects) used for persistence
    private final GenericDAO<Employee> employeeDAO;
    private final GenericDAO<Customer> customerDAO;

    // File Exporters
    private final JsonExporter jsonExporter;
    private final TextExporter textExporter;

    // Engine for complex calculations
    private final AnalyticsEngine analyticsEngine;

    // --- Constructor ---
    public ManagementService() {
        // Initialize the concrete DAO implementations
        this.employeeDAO = new EmployeeDAOImpl();
        this.customerDAO = new CustomerDAOImpl();

        // Initialize File Exporters
        this.jsonExporter = new JsonExporter();
        this.textExporter = new TextExporter();

        // Initialize Analytics Engine
        this.analyticsEngine = new AnalyticsEngine();
    }

    // =========================================================
    // CRUD OPERATIONS (Employee)
    // =========================================================

    public void saveEmployee(Employee employee) throws IllegalArgumentException {
        // Step 1: Validate the data before saving
        if (!validateEmployee(employee)) {
            // Throw a specific exception or handle the error
            throw new IllegalArgumentException("Employee validation failed. Name and Job Title must not be empty, Age must be > 18.");
        }

        // Step 2: Delegate the operation to the DAO
        employeeDAO.save(employee);
    }

    public Optional<Employee> getEmployeeById(String id) {
        // Use Optional for safe return of potentially null object
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
        if (!validateCustomer(customer)) {
            throw new IllegalArgumentException("Customer validation failed. Name and Membership Level must not be empty, Age must be > 18.");
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
        if (!validateCustomer(customer)) {
            throw new IllegalArgumentException("Customer validation failed during update.");
        }
        customerDAO.update(customer);
    }

    public boolean deleteCustomer(String id) {
        return customerDAO.delete(id);
    }

    // =========================================================
    // VALIDATION LOGIC (Business Rules)
    // =========================================================

    private boolean validateEmployee(Employee e) {
        // Example Rule 1: Basic string check
        if (e.getName() == null || e.getName().trim().isEmpty() ||
                e.getJobTitle() == null || e.getJobTitle().trim().isEmpty()) {
            return false;
        }
        // Example Rule 2: Business logic (Age check)
        if (e.getAge() < 18) {
            return false;
        }
        // Additional checks (salary > 0, ID format, etc.) would go here
        return true;
    }

    private boolean validateCustomer(Customer c) {
        // Example Rule: Customer name/membership must be present
        if (c.getName() == null || c.getName().trim().isEmpty() ||
                c.getMembershipLevel() == null || c.getMembershipLevel().trim().isEmpty()) {
            return false;
        }
        // Example Rule: Age check
        if (c.getAge() < 18) {
            return false;
        }
        // Additional checks (Last Purchase Date is not in the future, etc.) would go here
        return true;
    }

    // =========================================================
    // ANALYTICS INTEGRATION
    // =========================================================

    /**
     * Delegates the average age calculation to the AnalyticsEngine.
     */
    public double getAverageAge() {
        // Retrieve all employees and customers
        List<Person> people = (List<Person>) (List<?>) getAllEmployees();
        people.addAll(getAllCustomers());

        return analyticsEngine.calculateAverageAge(people);
    }

    /**
     * Delegates the average salary calculation to the AnalyticsEngine.
     */
    public double getAverageSalary() {
        return analyticsEngine.calculateAverageSalary(getAllEmployees());
    }

    /**
     * Delegates the membership breakdown calculation to the AnalyticsEngine.
     */
    public long getMembershipCount(String level) {
        return analyticsEngine.countMembershipLevel(getAllCustomers(), level);
    }

    // =========================================================
    // FILE EXPORT OPERATIONS
    // =========================================================

    /**
     * Exports all Employee data to a JSON file.
     * @param filename The path/name of the file.
     * @throws IOException If the file cannot be written.
     */
    public void exportEmployeesToJson(String filename) throws IOException {
        jsonExporter.export(getAllEmployees(), filename);
    }

    /**
     * Exports all Customer data to a Text/CSV file.
     * @param filename The path/name of the file.
     * @throws IOException If the file cannot be written.
     */
    public void exportCustomersToText(String filename) throws IOException {
        textExporter.export(getAllCustomers(), filename);
    }
}