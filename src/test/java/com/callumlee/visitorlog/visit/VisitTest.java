package com.callumlee.visitorlog.visit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.callumlee.visitorlog.resident.Resident;
import com.callumlee.visitorlog.visitor.Visitor;

class VisitTest {
    private LocalDateTime timeIn;
    private Visit visit;

    @BeforeEach
    void setUp() {
        timeIn = LocalDateTime.of(2026, 10, 9, 14, 0);
        visit = new Visit(new Visitor("Jane Doe", null), new Resident("Demo Resident", "101"), timeIn);
    }

    @Test
    void newVisitIsSignedIn() {
        assertTrue(visit.isSignedIn());
        assertNull(visit.getTimeOut());
        assertEquals(0, visit.getDurationMinutes());
    }

    @Test
    void signOutRecordsTimeAndDuration() {
        visit.signOut(timeIn.plusMinutes(45));
        assertFalse(visit.isSignedIn());
        assertEquals(timeIn.plusMinutes(45), visit.getTimeOut());
        assertEquals(45, visit.getDurationMinutes());
    }

    @Test
    void cannotSignOutTwice() {
        visit.signOut(timeIn.plusMinutes(10));
        assertThrows(IllegalStateException.class, () -> visit.signOut(timeIn.plusMinutes(20)));
        assertEquals(timeIn.plusMinutes(10), visit.getTimeOut());
    }

    @Test
    void cannotSignOutBeforeSignIn() {
        assertThrows(IllegalArgumentException.class, () -> visit.signOut(timeIn.minusMinutes(1)));
        assertTrue(visit.isSignedIn());
    }
}
