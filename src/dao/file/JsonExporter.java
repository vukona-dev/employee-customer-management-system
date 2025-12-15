package dao.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Person;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Handles the export of data to JSON format using the Gson library.
 * This class is generic and can handle any list of data.
 */
public class JsonExporter {

    private final Gson gson;

    public JsonExporter() {
        // GsonBuilder is used for pretty printing (readable JSON)
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Converts a list of objects to a JSON string and saves it to a file.
     * @param <T> The type of the objects in the list (e.g., Employee, Customer).
     * @param data The list of objects to export.
     * @param filename The path and name of the file to save the JSON to.
     * @throws IOException If there is an error writing to the file.
     */
    public <T extends Person> void export(List<T> data, String filename) throws IOException {
        // 1. Convert the List of objects into a JSON string
        String jsonString = gson.toJson(data);

        // 2. Write the JSON string to the specified file
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(jsonString);
        }
    }
}