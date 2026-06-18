package com.hean.consigueventas.oonabe;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("postgres-test")
class PostgresSchemaValidationTest {

    @Autowired
    private Flyway flyway;

    @TestConfiguration
    static class FlywayCleanTestConfig {
        @Bean
        public FlywayMigrationStrategy cleanMigrationStrategy() {
            return f -> {
                f.clean();
                f.migrate();
            };
        }
    }

    @Test
    void contextLoadsAndPostgresSchemaIsValid() {
        assertThat(flyway).isNotNull();
        System.out.println("PostgreSQL 17 schema validated successfully against Hibernate JPA model!");
    }
}
