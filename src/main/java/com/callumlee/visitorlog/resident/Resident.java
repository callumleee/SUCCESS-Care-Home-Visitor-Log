package com.callumlee.visitorlog.resident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// A resident of the care home. Residents who move out are deactivated, not deleted,
// so their visit history is kept.
@Entity
@Table(name = "residents")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String room;

    @Column(nullable = false)
    private boolean active = true;

    protected Resident() {
    }

    public Resident(String fullName, String room) {
        this.fullName = fullName;
        this.room = room;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoom() {
        return room;
    }

    public boolean isActive() {
        return active;
    }
}
