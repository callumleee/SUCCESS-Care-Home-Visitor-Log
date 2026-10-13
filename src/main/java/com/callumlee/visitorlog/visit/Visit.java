package com.callumlee.visitorlog.visit;

import java.time.Duration;
import java.time.LocalDateTime;

import com.callumlee.visitorlog.resident.Resident;
import com.callumlee.visitorlog.visitor.Visitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// A single visit: a visitor + a resident + sign-in/sign-out times.
// timeOut is null while the visitor is still in the building.
@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @Column(name = "time_in", nullable = false)
    private LocalDateTime timeIn;

    @Column(name = "time_out")
    private LocalDateTime timeOut;

    protected Visit() {
    }

    public Visit(Visitor visitor, Resident resident, LocalDateTime timeIn) {
        this.visitor = visitor;
        this.resident = resident;
        this.timeIn = timeIn;
    }

    // Records the sign-out time. Throws if the visit is already signed out
    // or the time is before the sign-in time.
    public void signOut(LocalDateTime time) {
        if (timeOut != null) {
            throw new IllegalStateException("Visit is already signed out");
        }
        if (time.isBefore(timeIn)) {
            throw new IllegalArgumentException("Sign-out time is before sign-in time");
        }
        this.timeOut = time;
    }

    public boolean isSignedIn() {
        return timeOut == null;
    }

    // Returns the visit length in minutes, or 0 if the visitor has not signed out yet
    public long getDurationMinutes() {
        if (timeOut == null) {
            return 0;
        }
        return Duration.between(timeIn, timeOut).toMinutes();
    }

    public Long getId() {
        return id;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public Resident getResident() {
        return resident;
    }

    public LocalDateTime getTimeIn() {
        return timeIn;
    }

    public LocalDateTime getTimeOut() {
        return timeOut;
    }
}
