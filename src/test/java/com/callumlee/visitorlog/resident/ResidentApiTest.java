package com.callumlee.visitorlog.resident;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import com.callumlee.visitorlog.ApiTestBase;

class ResidentApiTest extends ApiTestBase {

    @Autowired
    private ResidentRepository residents;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddResident() throws Exception {
        mvc.perform(post("/api/residents").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Demo Resident\",\"room\":\"101\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Demo Resident"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void frontDeskCannotAddResident() throws Exception {
        mvc.perform(post("/api/residents").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Demo Resident\",\"room\":\"101\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivatedResidentIsHiddenFromList() throws Exception {
        Resident stays = residents.save(new Resident("Resident A", "101"));
        Resident leaves = residents.save(new Resident("Resident B", "102"));

        mvc.perform(delete("/api/residents/{id}", leaves.getId())).andExpect(status().isNoContent());

        mvc.perform(get("/api/residents"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(stays.getId()));
    }
}
