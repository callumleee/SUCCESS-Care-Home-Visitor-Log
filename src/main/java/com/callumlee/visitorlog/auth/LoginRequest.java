package com.callumlee.visitorlog.auth;

import jakarta.validation.constraints.NotBlank;

// Body of POST /api/auth/login
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
