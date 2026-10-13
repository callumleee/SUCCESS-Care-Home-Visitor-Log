package persistence;

import model.VisitorLog;
import org.json.JSONObject;
import java.io.*;

//Citation: A solid portion of the structure is derived from the JsonSerializationDemo
// given via CPSC210 course materials.

// Represents a writer that writes a VisitorLog to a JSON file
public class JsonWriter {
    private PrintWriter writer;
    private String filePath;

    // EFFECTS: constructs a writer to write to the given file path
    public JsonWriter(String filePath) {
        this.filePath = filePath;
    }

    // MODIFIES: this
    // EFFECTS: opens the file for writing; throws FileNotFoundException if file cannot be opened
    public void open() throws FileNotFoundException {
        writer = new PrintWriter(new File(filePath));
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of the VisitorLog to file
    public void write(VisitorLog log) {
        JSONObject json = log.toJson();
        saveToFile(json.toString(2));  
    }

    // MODIFIES: this
    // EFFECTS: writes the given string to file
    private void saveToFile(String json) {
        writer.print(json);
    }

    // MODIFIES: this
    // EFFECTS: closes the writer if it is open
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
