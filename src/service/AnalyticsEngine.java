package service;

import model.Customer;
import model.Employee;
import model.Person;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * A dedicated utility class within the Service layer for complex calculations
 * (e.g., averaging, counting, summarizing).
 */
public class AnalyticsEngine {

    // =========================================================
    // EXISTING METHODS
    // =========================================================

    /**
     * Calculates the average age across a list of people (Employees and Customers).
     * @param people List of Person objects.
     * @return The average age, or 0.0 if the list is empty.
     */
    public double calculateAverageAge(List<Person> people) {
        if (people == null || people.isEmpty()) {
            return 0.0;
        }

        double totalAge = people.stream()
                .mapToInt(Person::getAge)
                .sum();

        return totalAge / people.size();
    }

    /**
     * Calculates the average salary across a list of employees.
     * @param employees List of Employee objects.
     * @return The average salary, or 0.0 if the list is empty.
     */
    public double calculateAverageSalary(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return 0.0;
        }

        double totalSalary = employees.stream()
                .mapToDouble(Employee::getSalary)
                .sum();

        return totalSalary / employees.size();
    }

    /**
     * Counts the number of customers with a specific membership level.
     * @param customers List of Customer objects.
     * @param level The membership level to count (e.g., "Gold").
     * @return The count of customers matching the level.
     */
    public long countMembershipLevel(List<Customer> customers, String level) {
        if (customers == null || level == null) {
            return 0;
        }

        return customers.stream()
                .filter(c -> level.equalsIgnoreCase(c.getMembershipLevel()))
                .count();
    }

    // =========================================================
    // NEW ANALYTICS METHODS FOR REPORTING PANEL
    // =========================================================

    /**
     * Calculates the average salary for each unique job title (for the bar chart data).
     * @param employees List of all employees.
     * @return A Map where keys are job titles (String) and values are average salaries (Double).
     */
    public Map<String, Double> getAverageSalaryByJobTitle(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return new HashMap<>();
        }

        // Group employees by job title
        Map<String, List<Employee>> employeesByJob =
                employees.stream().collect(Collectors.groupingBy(Employee::getJobTitle));

        Map<String, Double> results = new HashMap<>();

        // Calculate the average salary for each group
        for (Map.Entry<String, List<Employee>> entry : employeesByJob.entrySet()) {
            double avgSalary = entry.getValue().stream()
                    .mapToDouble(Employee::getSalary)
                    .average()
                    .orElse(0.0);
            results.put(entry.getKey(), avgSalary);
        }

        return results;
    }

    /**
     * Calculates the count of employees for each unique job title (for the bar chart).
     * @param employees List of all employees.
     * @return A Map where keys are job titles (String) and values are employee counts (Integer).
     */
    public Map<String, Integer> getEmployeeCountByJobTitle(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return new HashMap<>();
        }

        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getJobTitle, Collectors.summingInt(e -> 1)));
    }

    /**
     * Calculates the count of customers for each unique membership level (for the pie chart).
     * @param customers List of all customers.
     * @return A Map where keys are membership levels (String) and values are customer counts (Integer).
     */
    public Map<String, Integer> getCustomerCountByMembershipLevel(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            return new HashMap<>();
        }

        return customers.stream()
                .collect(Collectors.groupingBy(Customer::getMembershipLevel, Collectors.summingInt(c -> 1)));
    }
}