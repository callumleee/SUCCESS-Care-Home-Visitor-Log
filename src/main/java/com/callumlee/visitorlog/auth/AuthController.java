package com.callumlee.visitorlog.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.callumlee.visitorlog.staff.StaffUser;
import com.callumlee.visitorlog.staff.StaffUserRepository;

import jakarta.validation.Valid;

// Staff log in with a username and password and get back a signed token
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StaffUserRepository staff;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration expiry;

    public AuthController(StaffUserRepository staff, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
            Clock clock, @Value("${visitorlog.jwt.expiry-hours}") long expiryHours) {
        this.staff = staff;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.expiry = Duration.ofHours(expiryHours);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        StaffUser user = staff.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password"));

        Instant now = clock.instant();
        Instant expiresAt = now.plus(expiry);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("visitorlog")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, user.getUsername(), user.getRole(), expiresAt);
    }
}
