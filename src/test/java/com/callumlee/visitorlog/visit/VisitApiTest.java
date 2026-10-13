package com.callumlee.visitorlog.visit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import com.callumlee.visitorlog.ApiTestBase;
import com.callumlee.visitorlog.resident.Resident;
import com.callumlee.visitorlog.resident.ResidentRepository;

@WithMockUser(roles = "FRONT_DESK")
class VisitApiTest extends ApiTestBase {

    @Autowired
    private ResidentRepository residents;

    @Autowired
    private VisitRepository visits;

    private Resident resident;

    @BeforeEach
    void setUp() {
        resident = residents.save(new Resident("Demo Resident", "101"));
    }

    @Test
    void signInCreatesVisit() throws Exception {
        signIn("Jane Doe", "604-555-1234", resident.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.visitorName").value("Jane Doe"))
                .andExpect(jsonPath("$.residentName").value("Demo Resident"))
                .andExpect(jsonPath("$.room").value("101"))
                .andExpect(jsonPath("$.signedIn").value(true));
    }

    @Test
    void signInWithUnknownResidentReturns404() throws Exception {
        signIn("Jane Doe", null, 999_999L).andExpect(status().isNotFound());
    }

    @Test
    void signInWithBlankNameReturns400() throws Exception {
        signIn(" ", null, resident.getId()).andExpect(status().isBadRequest());
    }

    @Test
    void signInTwiceToSameResidentReturns409() throws Exception {
        signIn("Jane Doe", "604-555-1234", resident.getId()).andExpect(status().isCreated());
        // Same person, phone written differently
        signIn("jane doe", "(604) 555 1234", resident.getId()).andExpect(status().isConflict());
    }

    @Test
    void signOutEndsVisit() throws Exception {
        long id = createVisit("Jane Doe");

        mvc.perform(patch("/api/visits/{id}/signout", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signedIn").value(false))
                .andExpect(jsonPath("$.timeOut").isNotEmpty());
    }

    @Test
    void signOutTwiceReturns409() throws Exception {
        long id = createVisit("Jane Doe");
        mvc.perform(patch("/api/visits/{id}/signout", id)).andExpect(status().isOk());
        mvc.perform(patch("/api/visits/{id}/signout", id)).andExpect(status().isConflict());
    }

    @Test
    void signOutUnknownVisitReturns404() throws Exception {
        mvc.perform(patch("/api/visits/{id}/signout", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void activeListOnlyShowsVisitorsStillInBuilding() throws Exception {
        long left = createVisit("Visitor One");
        createVisit("Visitor Two");
        mvc.perform(patch("/api/visits/{id}/signout", left)).andExpect(status().isOk());

        mvc.perform(get("/api/visits").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].visitorName").value("Visitor Two"));

        mvc.perform(get("/api/visits")).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void dateFilterOnlyReturnsVisitsFromThatDay() throws Exception {
        createVisit("Jane Doe");
        LocalDate today = visits.findAll().get(0).getTimeIn().toLocalDate();

        mvc.perform(get("/api/visits").param("date", today.toString()))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/visits").param("date", today.minusDays(1).toString()))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void residentVisitHistory() throws Exception {
        createVisit("Jane Doe");
        Resident other = residents.save(new Resident("Other Resident", "102"));
        signIn("John Doe", null, other.getId()).andExpect(status().isCreated());

        mvc.perform(get("/api/residents/{id}/visits", resident.getId()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].visitorName").value("Jane Doe"));
    }

    private long createVisit(String visitorName) throws Exception {
        String json = signIn(visitorName, null, resident.getId())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private ResultActions signIn(String name, String phone, Long residentId) throws Exception {
        String phoneJson = phone == null ? "null" : "\"" + phone + "\"";
        String body = "{\"visitorName\":\"" + name + "\",\"visitorPhone\":" + phoneJson
                + ",\"residentId\":" + residentId + "}";
        return mvc.perform(post("/api/visits").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
