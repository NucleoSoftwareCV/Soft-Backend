package com.hean.consigueventas.oonabe.notification.config;

import com.hean.consigueventas.oonabe.notification.domain.NotificationType;
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

/**
 * Las bases locales previas a la agenda pueden conservar columnas con longitudes
 * menores. Hibernate no siempre amplía esas columnas con ddl-auto=update, por lo
 * que esta migración no destructiva las adapta antes de recibir nuevas citas.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class NotificationSchemaMigration implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!isPostgreSql(connection)) {
                return;
            }
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                if (tableExists(connection, "session_bookings")) {
                    statement.execute("ALTER TABLE session_bookings ALTER COLUMN status TYPE VARCHAR(30)");
                }
                if (tableExists(connection, "notifications")) {
                    statement.execute("ALTER TABLE notifications ALTER COLUMN type TYPE VARCHAR(40)");
                    statement.execute("ALTER TABLE notifications ALTER COLUMN title TYPE VARCHAR(120)");
                    statement.execute("ALTER TABLE notifications ALTER COLUMN message TYPE VARCHAR(255)");
                    statement.execute("ALTER TABLE notifications ALTER COLUMN event_title TYPE VARCHAR(180)");
                    statement.execute("ALTER TABLE notifications ALTER COLUMN booking_code TYPE VARCHAR(30)");
                    statement.execute("ALTER TABLE notifications ALTER COLUMN action_label TYPE VARCHAR(40)");
                    if (!containsAllTypes(notificationTypeConstraint(connection))) {
                        statement.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
                        statement.execute("ALTER TABLE notifications ADD CONSTRAINT notifications_type_check CHECK (type IN ("
                                + allowedTypesSql() + "))");
                    }
                }
                connection.commit();
                log.info("Verified appointment and notification column lengths");
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    private boolean isPostgreSql(Connection connection) throws Exception {
        return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgresql");
    }

    private boolean tableExists(Connection connection, String tableName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = current_schema() AND table_name = ?
                """)) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

    private String notificationTypeConstraint(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT pg_get_constraintdef(constraint_row.oid)
                FROM pg_constraint constraint_row
                JOIN pg_class table_row ON table_row.oid = constraint_row.conrelid
                JOIN pg_namespace schema_row ON schema_row.oid = table_row.relnamespace
                WHERE schema_row.nspname = current_schema()
                  AND table_row.relname = 'notifications'
                  AND constraint_row.conname = 'notifications_type_check'
                """)) {
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    static boolean containsAllTypes(String definition) {
        return definition != null && Arrays.stream(NotificationType.values())
                .map(Enum::name)
                .allMatch(definition::contains);
    }

    static String allowedTypesSql() {
        return Arrays.stream(NotificationType.values())
                .map(type -> "'" + type.name() + "'")
                .collect(Collectors.joining(", "));
    }
}
