package com.hean.consigueventas.oonabe.profileProfesional.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpecialistProfileCityBackfill implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        jdbc.update("""
                UPDATE specialist_profiles p
                SET city_id = (
                    SELECT a.city_id FROM role_promotion_requests a WHERE a.user_id = p.user_id
                )
                WHERE p.city_id IS NULL AND EXISTS (
                    SELECT 1 FROM role_promotion_requests a
                    WHERE a.user_id = p.user_id AND a.status = 'APROBADO' AND a.city_id IS NOT NULL
                )
                """);
    }
}
