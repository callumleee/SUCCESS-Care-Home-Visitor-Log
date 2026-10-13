package com.callumlee.visitorlog.common;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Provides the clock used for sign-in/sign-out times, in the care home's time zone.
// Injecting a Clock (instead of calling LocalDateTime.now()) makes times testable.
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(@Value("${visitorlog.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
