package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting data from the application model lists to CSV files.
 */
public class DataExporter {

    /**
     * Exports a list of string arrays (representing rows) to a CSV file.
     * @param filePath The full path to the output CSV file.
     * @param header The header row (e.g., {"ID", "Name", "Salary"}).
     * @param dataRows The list of data rows, where each row is a String array.
     * @throws IOException If there is an error writing to the file.
     */
    public static void exportToCsv(String filePath, String[] header, List<String[]> dataRows) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write Header
            writer.write(convertToCsvRow(header));

            // Write Data Rows
            for (String[] row : dataRows) {
                writer.write(convertToCsvRow(row));
            }

            System.out.println("Data successfully exported to: " + filePath);
        }
    }

    /**
     * Helper method to convert a String array into a comma-separated, quoted CSV line.
     */
    private static String convertToCsvRow(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            // Enclose fields in double quotes and escape existing quotes for safety
            sb.append("\"").append(row[i].replace("\"", "\"\"")).append("\"");

            if (i < row.length - 1) {
                sb.append(",");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}