package com.callumlee.visitorlog.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.callumlee.visitorlog.ApiTestBase;

class AuthApiTest extends ApiTestBase {

    @Test
    void loginReturnsTokenThatWorks() throws Exception {
        String json = login("admin", "dev-admin-password")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();
        String token = json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        login("admin", "wrong-password").andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mvc.perform(get("/api/visits").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void browserFromAllowedOriginPassesCorsCheck() throws Exception {
        mvc.perform(options("/api/visits")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    private org.springframework.test.web.servlet.ResultActions login(String username, String password)
            throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
    }
}
