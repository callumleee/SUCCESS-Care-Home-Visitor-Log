package persistence;

import model.VisitorEntry;
import model.VisitorLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.json.*;

//Citation: A solid portion of the structure is derived from the JsonSerializationDemo
// given via CPSC210 course materials.

// Represents a reader that reads VisitorLog data from JSON stored in file
public class JsonReader {
    private String filePath;

    // EFFECTS: constructs reader to read from given file path
    public JsonReader(String filePath) {
        this.filePath = filePath;
    }

    // EFFECTS: reads VisitorLog from file and returns it
    // throws IOException if an error occurs reading data from file
    public VisitorLog read() throws IOException {
        String jsonData = readFile(filePath);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseVisitorLog(jsonObject);
    }

    // EFFECTS: reads source file as string and returns it
    private String readFile(String path) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s));
        }

        return contentBuilder.toString();
    }

    // EFFECTS: parses VisitorLog from JSON object and returns it
    private VisitorLog parseVisitorLog(JSONObject jsonObject) {
        VisitorLog log = new VisitorLog();
        addEntries(log, jsonObject);
        return log;
    }

    // MODIFIES: log
    // EFFECTS: parses entries from JSON object and adds them to VisitorLog
    private void addEntries(VisitorLog log, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("entries");
        for (Object json : jsonArray) {
            JSONObject nextEntry = (JSONObject) json;
            addEntry(log, nextEntry);
        }
    }

    // MODIFIES: log
    // EFFECTS: parses a single VisitorEntry from JSON object and adds it to VisitorLog
    private void addEntry(VisitorLog log, JSONObject jsonObject) {
        String visitorName = jsonObject.getString("visitorName");
        String residentName = jsonObject.getString("residentName");
        LocalDateTime timeIn = LocalDateTime.parse(jsonObject.getString("timeIn"));

        VisitorEntry entry = new VisitorEntry(visitorName, residentName, timeIn);

        if (!jsonObject.isNull("timeOut")) {
            LocalDateTime timeOut = LocalDateTime.parse(jsonObject.getString("timeOut"));
            entry.signOut(timeOut);
        }

        log.addEntry(entry);
    }
}
