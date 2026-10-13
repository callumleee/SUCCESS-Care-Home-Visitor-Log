package model;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class VisitorEntryTest {
    private VisitorEntry entry;
    private LocalDateTime timeIn;

    @BeforeEach
    void runBefore() {
        timeIn = LocalDateTime.of(2025, 10, 9, 22, 0);
        entry = new VisitorEntry("Callum", "Resident A", timeIn);
    }

    @Test
    void testConstructor() {
        assertEquals("Callum", entry.getVisitorName());
        assertEquals("Resident A", entry.getResidentName());
        assertEquals(timeIn, entry.getTimeIn());
        assertNull(entry.getTimeOut());
        assertTrue(entry.isSignedIn());
        assertEquals(0, entry.getDurationMinutes());
    }

    @Test
    void testSignOutChangesState() {
        LocalDateTime timeOut = timeIn.plusMinutes(45);
        entry.signOut(timeOut);
        assertFalse(entry.isSignedIn());
        assertEquals(timeOut, entry.getTimeOut());
        assertEquals(45, entry.getDurationMinutes());
    }

    @Test
    void testGetDurationReturnsZeroIfStillSignedIn() {
        assertTrue(entry.isSignedIn());
        assertEquals(0, entry.getDurationMinutes());
    }

    @Test
    void testSignOutWithNullDoesNothing() {
        entry.signOut(null); 
        assertTrue(entry.isSignedIn());
        assertNull(entry.getTimeOut());
        assertEquals(0, entry.getDurationMinutes());
    }

}
