package com.hean.consigueventas.oonabe.booking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** ddl-auto=update no amplía el CHECK que Hibernate creó sobre event_bookings.status. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class BookingStatusSchemaMigration implements ApplicationRunner {
    private static final String CONSTRAINT = "event_bookings_status_check";

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        String product = jdbc.execute((ConnectionCallback<String>) c -> c.getMetaData().getDatabaseProductName());
        if (!"PostgreSQL".equals(product)) {
            return;
        }
        String definition = jdbc.query(
                "select pg_get_constraintdef(oid) from pg_constraint where conname = ? and conrelid = to_regclass('event_bookings')",
                rs -> rs.next() ? rs.getString(1) : null, CONSTRAINT);
        if (definition == null || definition.contains("ACEPTADA")) {
            return;
        }
        jdbc.execute("ALTER TABLE event_bookings DROP CONSTRAINT " + CONSTRAINT);
        jdbc.execute("ALTER TABLE event_bookings ADD CONSTRAINT " + CONSTRAINT + " CHECK (status IN "
                + "('PENDIENTE','ACEPTADA','CONFIRMADA','CANCELADA','LISTA_ESPERA','COMPLETADA','NO_ASISTIO'))");
        log.info("Restricción {} ampliada con el estado ACEPTADA", CONSTRAINT);
    }
}
