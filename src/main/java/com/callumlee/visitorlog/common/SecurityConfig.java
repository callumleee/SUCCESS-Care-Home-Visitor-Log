package com.callumlee.visitorlog.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Every endpoint requires a staff login (HTTP Basic for now).
// Front desk: sign visitors in/out and view visits/residents.
// Admin: everything, including managing residents and staff, and stats.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // stateless API with no cookies, so CSRF does not apply
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/stats/**", "/api/staff/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/residents").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/residents/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("FRONT_DESK", "ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
