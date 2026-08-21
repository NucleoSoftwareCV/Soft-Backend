package com.hean.consigueventas.oonabe.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Madrid");

    @Bean
    public Clock businessClock() {
        return Clock.system(BUSINESS_ZONE);
    }
}
