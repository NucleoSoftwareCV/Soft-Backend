package com.hean.consigueventas.oonabe.booking.config;

import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class BookingStatusSchemaMigration implements ApplicationRunner {

    static final String TABLE_NAME = "session_bookings";
    static final String CONSTRAINT_NAME = "session_bookings_status_check";

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!isPostgreSql(connection) || !tableExists(connection)) {
                return;
            }

            String definition = constraintDefinition(connection);
            if (definition != null && containsAllStatuses(definition)) {
                return;
            }

            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + TABLE_NAME
                        + " DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME);
                statement.execute("ALTER TABLE " + TABLE_NAME
                        + " ADD CONSTRAINT " + CONSTRAINT_NAME
                        + " CHECK (status IN (" + allowedStatusesSql() + "))");
                connection.commit();
                log.info("Updated {} to include every BookingStatus value", CONSTRAINT_NAME);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    private boolean isPostgreSql(Connection connection) throws Exception {
        return connection.getMetaData().getDatabaseProductName()
                .toLowerCase(Locale.ROOT)
                .contains("postgresql");
    }

    private boolean tableExists(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = current_schema()
                  AND table_name = ?
                """)) {
            statement.setString(1, TABLE_NAME);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

    private String constraintDefinition(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT pg_get_constraintdef(constraint_row.oid)
                FROM pg_constraint constraint_row
                JOIN pg_class table_row ON table_row.oid = constraint_row.conrelid
                JOIN pg_namespace schema_row ON schema_row.oid = table_row.relnamespace
                WHERE schema_row.nspname = current_schema()
                  AND table_row.relname = ?
                  AND constraint_row.conname = ?
                """)) {
            statement.setString(1, TABLE_NAME);
            statement.setString(2, CONSTRAINT_NAME);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    static boolean containsAllStatuses(String definition) {
        return Arrays.stream(BookingStatus.values())
                .map(Enum::name)
                .allMatch(definition::contains);
    }

    static String allowedStatusesSql() {
        return Arrays.stream(BookingStatus.values())
                .map(status -> "'" + status.name() + "'")
                .collect(Collectors.joining(", "));
    }
}
