package service;

import model.Customer;
import model.Employee;
import model.Person;
import java.util.List;

/**
 * A dedicated utility class within the Service layer for complex calculations
 * (e.g., averaging, counting, summarizing).
 */
public class AnalyticsEngine {

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
}