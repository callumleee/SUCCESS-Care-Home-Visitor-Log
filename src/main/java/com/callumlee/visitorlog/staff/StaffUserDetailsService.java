package com.callumlee.visitorlog.staff;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Lets Spring Security look up staff logins in the database
@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffUserRepository staff;

    public StaffUserDetailsService(StaffUserRepository staff) {
        this.staff = staff;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        StaffUser user = staff.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
