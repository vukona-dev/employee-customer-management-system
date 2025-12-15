package dao.file;

import model.Customer;
import model.Employee;
import model.Person;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles the export of data to a simple Text/CSV format.
 * This requires specific logic for each entity type (Employee vs. Customer).
 */
public class TextExporter {

    /**
     * Exports a list of Person objects to a text file.
     * @param data The list of objects to export.
     * @param filename The path and name of the file to save the text to.
     * @throws IOException If there is an error writing to the file.
     */
    public <T extends Person> void export(List<T> data, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

            if (data == null || data.isEmpty()) {
                writer.println("No data to export.");
                return;
            }

            // Determine the type of data and write a header row
            if (data.get(0) instanceof Employee) {
                writer.println("ID,Name,Age,Salary,JobTitle");
                writeEmployeeData(writer, (List<Employee>) data);
            } else if (data.get(0) instanceof Customer) {
                writer.println("ID,Name,Age,MembershipLevel,LastPurchaseDate");
                writeCustomerData(writer, (List<Customer>) data);
            }
        }
    }

    private void writeEmployeeData(PrintWriter writer, List<Employee> employees) {
        for (Employee emp : employees) {
            String line = String.format("%s,%s,%d,%.2f,%s",
                    emp.getId(),
                    emp.getName(),
                    emp.getAge(),
                    emp.getSalary(),
                    emp.getJobTitle()
            );
            writer.println(line);
        }
    }

    private void writeCustomerData(PrintWriter writer, List<Customer> customers) {
        for (Customer cust : customers) {
            String line = String.format("%s,%s,%d,%s,%s",
                    cust.getId(),
                    cust.getName(),
                    cust.getAge(),
                    cust.getMembershipLevel(),
                    cust.getLastPurchaseDate().toString() // Write LocalDate as ISO-8601 string
            );
            writer.println(line);
        }
    }
}