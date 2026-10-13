package persistence;

import model.VisitorEntry;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


// Citation: Structure adapted from the CPSC210 JsonSerializationDemo.
public class JsonTest {
    protected void checkVisitorEntry(String visitorName, String residentName, LocalDateTime timeIn,
            LocalDateTime timeOut, VisitorEntry entry) {
        assertEquals(visitorName, entry.getVisitorName());
        assertEquals(residentName, entry.getResidentName());
        assertEquals(timeIn, entry.getTimeIn());
        assertEquals(timeOut, entry.getTimeOut());
    }
}
