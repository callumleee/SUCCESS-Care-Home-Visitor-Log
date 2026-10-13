package persistence;

import org.json.JSONObject;

// Represents data that can be written to JSON
public interface Writable {
    // EFFECTS: returns this object as a JSONObject
    JSONObject toJson();
}
