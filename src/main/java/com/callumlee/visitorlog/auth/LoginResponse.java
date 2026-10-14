package com.callumlee.visitorlog.auth;

import java.time.Instant;

import com.callumlee.visitorlog.staff.Role;

// Returned after a successful login. The browser sends the token as
// "Authorization: Bearer <token>" on later requests.
public record LoginResponse(String token, String username, Role role, Instant expiresAt) {
}
