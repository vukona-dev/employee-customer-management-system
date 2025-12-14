package model;

/**
 * Represents an employee in the ECMS. Inherits common attributes from Person.
 * Implements the displayDetails method for polymorphic behavior.
 */
public class Employee extends Person {

    private double salary;
    private String jobTitle;

    // --- Constructor ---
    public Employee(String id, String name, int age, double salary, String jobTitle) {
        // Calls the constructor of the base class (Person)
        super(id, name, age);
        this.salary = salary;
        this.jobTitle = jobTitle;
    }

    // --- Polymorphic Implementation ---
    /**
     * Provides a detailed string representation specific to an Employee.
     */
    @Override
    public String displayDetails() {
        return String.format(
                "Employee Details: [%s], Salary: $%,.2f, Title: %s",
                super.toString(), // Uses the Person's toString() method
                salary,
                jobTitle
        );
    }

    // --- Getters and Setters Specific to Employee ---

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}