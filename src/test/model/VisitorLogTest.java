package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class VisitorLogTest {

    private VisitorLog log;
    private VisitorEntry entry1;
    private VisitorEntry entry2;
    private VisitorEntry entry3;
    private LocalDateTime timeIn;

    @BeforeEach
    void runBefore() {
        log = new VisitorLog();
        timeIn = LocalDateTime.of(2025, 10, 9, 22, 0);
        entry1 = new VisitorEntry("Callum", "Resident A", timeIn);
        entry2 = new VisitorEntry("Joe", "Resident B", timeIn.plusMinutes(10));
        entry3 = new VisitorEntry("Karen", "Resident C", timeIn.plusMinutes(20));
    }

    @Test
    void testConstructor() {
        assertEquals(0, log.getTotalEntries());
        assertTrue(log.getAllEntries().isEmpty());
    }

    @Test
    void testAddEntryIncreasesSize() {
        log.addEntry(entry1);
        log.addEntry(entry2);
        assertEquals(2, log.getTotalEntries());
        assertTrue(log.getAllEntries().contains(entry1));
        assertTrue(log.getAllEntries().contains(entry2));
    }

    @Test
    void testAddEntryNull() {
        log.addEntry(null);
        assertEquals(0, log.getTotalEntries());
        assertFalse(log.getAllEntries().contains(entry1));
        assertFalse(log.getAllEntries().contains(entry2));
    }

    @Test
    void testGetActiveVisitors() {
        log.addEntry(entry1);
        log.addEntry(entry2);
        entry2.signOut(timeIn.plusMinutes(40));

        List<VisitorEntry> active = log.getActiveVisitors();
        assertEquals(1, active.size());
        assertTrue(active.contains(entry1));
        assertFalse(active.contains(entry2));
    }

    @Test
    void testGetAverageVisitLengthNoCompletedVisits() {
        log.addEntry(entry1);
        log.addEntry(entry2);
        assertEquals(0.0, log.getAverageVisitLength());
    }

    @Test
    void testGetAverageVisitLengthWithCompletedVisits() {
        log.addEntry(entry1);
        log.addEntry(entry2);
        entry1.signOut(timeIn.plusMinutes(30));
        entry2.signOut(timeIn.plusMinutes(90));
        assertEquals(55.0, log.getAverageVisitLength());
    }

    @Test
    void testGetMostFrequentVisitorNoVisits() {
        assertNull(log.getMostFrequentVisitor());
    }

    @Test
    void testGetMostFrequentVisitorWithVisits() {
        log.addEntry(entry1);
        log.addEntry(entry2);
        log.addEntry(entry3);
        log.addEntry(new VisitorEntry("Callum", "Resident D", timeIn.plusMinutes(50)));
        assertEquals("Callum", log.getMostFrequentVisitor());
    }

    @Test
    void testGetMostVisitedResidentNoVisits() {
        assertNull(log.getMostVisitedResident());
    }

    @Test
    void testGetMostVisitedResidentWithVisits() {
        log.addEntry(entry1); 
        log.addEntry(entry2);
        log.addEntry(entry3);
        log.addEntry(new VisitorEntry("NewVisitor", "Resident A", timeIn.plusMinutes(50)));
        assertEquals("Resident A", log.getMostVisitedResident());
    }

    



    @Test
    void testConstructorWithEntries() {
        VisitorLog loaded = new VisitorLog(List.of(entry1, entry2));
        assertEquals(2, loaded.getTotalEntries());
        assertEquals(entry1, loaded.getAllEntries().get(0));
        assertEquals(entry2, loaded.getAllEntries().get(1));
    }
}
