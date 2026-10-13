package com.callumlee.visitorlog.staff;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.callumlee.visitorlog.ApiTestBase;

// Uses real logins (the admin account created on startup) instead of mock users
class SecurityApiTest extends ApiTestBase {
    private static final String ADMIN_PASSWORD = "dev-admin-password";

    @Test
    void requestWithoutLoginReturns401() throws Exception {
        mvc.perform(get("/api/visits")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mvc.perform(get("/api/visits").with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateFrontDeskAccountThatCanLogIn() throws Exception {
        mvc.perform(post("/api/staff").with(httpBasic("admin", ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"desk1\",\"password\":\"desk-password\",\"role\":\"FRONT_DESK\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("FRONT_DESK"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(get("/api/visits").with(httpBasic("desk1", "desk-password")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/stats").with(httpBasic("desk1", "desk-password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateUsernameReturns409() throws Exception {
        mvc.perform(post("/api/staff").with(httpBasic("admin", ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"another-password\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isConflict());
    }
}
