package com.callumlee.visitorlog.stats;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.callumlee.visitorlog.ApiTestBase;
import com.callumlee.visitorlog.resident.Resident;
import com.callumlee.visitorlog.resident.ResidentRepository;
import com.callumlee.visitorlog.visit.Visit;
import com.callumlee.visitorlog.visit.VisitRepository;
import com.callumlee.visitorlog.visitor.Visitor;
import com.callumlee.visitorlog.visitor.VisitorRepository;

class StatsApiTest extends ApiTestBase {

    @Autowired
    private ResidentRepository residents;

    @Autowired
    private VisitorRepository visitors;

    @Autowired
    private VisitRepository visits;

    @Test
    @WithMockUser(roles = "ADMIN")
    void statsWithNoVisits() throws Exception {
        mvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").value(0))
                .andExpect(jsonPath("$.averageVisitMinutes").value(0.0))
                .andExpect(jsonPath("$.mostVisitedResident").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void statsSummarizeVisits() throws Exception {
        Resident a = residents.save(new Resident("Resident A", "101"));
        Resident b = residents.save(new Resident("Resident B", "102"));
        Visitor jane = visitors.save(new Visitor("Jane Doe", null));
        Visitor john = visitors.save(new Visitor("John Doe", null));
        LocalDateTime t = LocalDateTime.of(2026, 10, 9, 14, 0);

        saveVisit(jane, a, t, 30);
        saveVisit(jane, b, t, 60);
        saveVisit(john, a, t, -1); // still signed in

        mvc.perform(get("/api/stats"))
                .andExpect(jsonPath("$.totalVisits").value(3))
                .andExpect(jsonPath("$.currentlySignedIn").value(1))
                .andExpect(jsonPath("$.averageVisitMinutes").value(45.0))
                .andExpect(jsonPath("$.mostVisitedResident").value("Resident A"))
                .andExpect(jsonPath("$.mostFrequentVisitor").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void frontDeskCannotSeeStats() throws Exception {
        mvc.perform(get("/api/stats")).andExpect(status().isForbidden());
    }

    // minutes < 0 means the visitor has not signed out
    private void saveVisit(Visitor visitor, Resident resident, LocalDateTime timeIn, int minutes) {
        Visit visit = new Visit(visitor, resident, timeIn);
        if (minutes >= 0) {
            visit.signOut(timeIn.plusMinutes(minutes));
        }
        visits.save(visit);
    }
}
