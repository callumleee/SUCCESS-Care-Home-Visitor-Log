package com.callumlee.visitorlog.staff;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.callumlee.visitorlog.common.ConflictException;

import jakarta.validation.Valid;

// Admin-only: create and list staff accounts
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffUserRepository staff;
    private final PasswordEncoder passwordEncoder;

    public StaffController(StaffUserRepository staff, PasswordEncoder passwordEncoder) {
        this.staff = staff;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<StaffResponse> list() {
        return staff.findAll().stream().map(StaffResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse create(@Valid @RequestBody StaffRequest request) {
        if (staff.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        StaffUser user = new StaffUser(request.username(), passwordEncoder.encode(request.password()),
                request.role());
        return StaffResponse.from(staff.save(user));
    }
}
