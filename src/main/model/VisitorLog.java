package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

// Represents a log of all visitor entries in the care home.
// Add entries, view all entries, list active visitors,
// calculate average visit duration, and find the most frequent visitor.
public class VisitorLog implements Writable {
    private List<VisitorEntry> entries;

    // EFFECTS: constructs an empty visitor log
    public VisitorLog() {
        entries = new ArrayList<>();
    }

    // MODIFIES: this
    // EFFECTS: adds the given visitor entry to the log
    public void addEntry(VisitorEntry entry) {
        if (entry != null) {
            entries.add(entry);
            EventLog.getInstance().logEvent(new Event("Visitor signed in: " + entry.getVisitorName()
                    + " visiting " + entry.getResidentName()));
        }
    }

    // EFFECTS: returns a list of all visitor entries in the log
    public List<VisitorEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    // EFFECTS: returns a list of all visitors who are currently signed in
    public List<VisitorEntry> getActiveVisitors() {
        List<VisitorEntry> active = new ArrayList<>();
        for (VisitorEntry e : entries) {
            if (e.isSignedIn()) {
                active.add(e);
            }
        }
        return active;
    }

    // EFFECTS: returns the average duration (in minutes) of all completed visits
    // returns 0 if there are no completed visits
    public double getAverageVisitLength() {
        long totalMinutes = 0;
        int completedCount = 0;

        for (VisitorEntry e : entries) {
            if (!e.isSignedIn()) {
                totalMinutes += e.getDurationMinutes();
                completedCount++;
            }
        }

        if (completedCount == 0) {
            return 0.0;
        }

        return (double) totalMinutes / completedCount;
    }

    // EFFECTS: returns the name of the visitor who has visited most frequently
    // null if there are no visits recorded
    public String getMostFrequentVisitor() {
        if (entries.isEmpty()) {
            return null;
        }

        Map<String, Integer> counts = new HashMap<>();
        for (VisitorEntry e : entries) {
            String visitor = e.getVisitorName();
            counts.put(visitor, counts.getOrDefault(visitor, 0) + 1);
        }

        String mostFrequent = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }

        return mostFrequent;
    }

    // EFFECTS: returns the name of the resident who has been visited most
    // frequently,
    // or null if no visits are recorded
    public String getMostVisitedResident() {
        if (entries.isEmpty()) {
            return null;
        }

        Map<String, Integer> counts = new HashMap<>();
        for (VisitorEntry e : entries) {
            String resident = e.getResidentName();
            counts.put(resident, counts.getOrDefault(resident, 0) + 1);
        }

        String mostVisited = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostVisited = entry.getKey();
            }
        }

        return mostVisited;
    }

    // EFFECTS: returns number of visitor entries in the log
    public int getTotalEntries() {
        return entries.size();
    }

    // EFFECTS: returns this VisitorLog as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("entries", entriesToJson());
        return json;
    }

    // EFFECTS: returns entries in this log as a JSON array
    private JSONArray entriesToJson() {
        JSONArray jsonArray = new JSONArray();
        for (VisitorEntry e : entries) {
            jsonArray.put(e.toJson());
        }
        return jsonArray;
    }
}
