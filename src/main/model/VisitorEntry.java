package model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.json.JSONObject;
import persistence.Writable;

//  Represents a single visit to a resident: visitor + resident + entry/exit times.
public class VisitorEntry implements Writable {
    private String visitorName;
    private String residentName;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;

    // EFFECTS: constructs a VisitorEntry with the given visitor name, resident
    // name,
    // and sign-in time, and sets timeOut to null to indicate the visitor has not
    // signed out yet
    public VisitorEntry(String visitorName, String residentName, LocalDateTime timeIn) {
        this.visitorName = visitorName;
        this.residentName = residentName;
        this.timeIn = timeIn;
        this.timeOut = null;

    }

    // MODIFIES: this
    // EFFECTS: sets timeOut to the given time if it is not null, leaving it
    // unchanged otherwise
    public void signOut(LocalDateTime timeOut) {
        if (timeOut != null) {
            this.timeOut = timeOut;
            EventLog.getInstance().logEvent(new Event("Visitor signed out: " + visitorName));
        }

    }

    // EFFECTS: returns true if the visitor is still signed in (timeOut is null) and
    // false otherwise
    public boolean isSignedIn() {
        return timeOut == null;
    }

    // EFFECTS: if the visitor has signed out, returns the number of minutes between
    // timeIn and timeOut,
    // otherwise returns 0
    public long getDurationMinutes() {
        if (timeOut == null) {
            return 0;
        }
        return Duration.between(timeIn, timeOut).toMinutes();
    }

    // EFFECTS: returns visitor name
    public String getVisitorName() {
        return visitorName;
    }

    // EFFECTS: returns resident name
    public String getResidentName() {
        return residentName;
    }

    // EFFECTS: returns the recorded sign-in time
    public LocalDateTime getTimeIn() {
        return timeIn;
    }

    // EFFECTS: returns the recorded sign-out time (null if still signed in)
    public LocalDateTime getTimeOut() {
        return timeOut;
    }

    // EFFECTS: returns this VisitorEntry as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("visitorName", visitorName);
        json.put("residentName", residentName);
        json.put("timeIn", timeIn.toString());

        if (timeOut == null) {
            json.put("timeOut", JSONObject.NULL);
        } else {
            json.put("timeOut", timeOut.toString());
        }

        return json;
    }

}
