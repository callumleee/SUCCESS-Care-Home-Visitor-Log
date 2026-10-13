package com.callumlee.visitorlog.staff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Creates the first admin account on startup if there are no staff accounts yet
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final StaffUserRepository staff;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminSeeder(StaffUserRepository staff, PasswordEncoder passwordEncoder,
            @Value("${visitorlog.seed.admin-username}") String username,
            @Value("${visitorlog.seed.admin-password}") String password) {
        this.staff = staff;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (staff.count() == 0) {
            staff.save(new StaffUser(username, passwordEncoder.encode(password), Role.ADMIN));
            log.info("Created initial admin account '{}'", username);
        }
    }
}
