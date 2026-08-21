package com.hean.consigueventas.oonabe.profileProfesional.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class LegacyProfessionalTaxonomySchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        migrateJoinTable(
                "professional_work_topics",
                "work_topic_id",
                "uk_professional_work_topic_profile");
        migrateJoinTable(
                "professional_techniques",
                "technique_id",
                "uk_professional_technique_profile");
    }

    private void migrateJoinTable(String tableName, String relatedColumn, String indexName) {
        if (!columnExists(tableName, "specialist_id")
                || !columnExists(tableName, "specialist_profile_id")) {
            return;
        }

        log.info("Migrating legacy column {}.specialist_id", tableName);
        jdbcTemplate.update("UPDATE " + tableName
                + " SET specialist_profile_id = specialist_id"
                + " WHERE specialist_profile_id IS NULL");
        jdbcTemplate.execute("ALTER TABLE " + tableName
                + " ALTER COLUMN specialist_profile_id SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE " + tableName
                + " DROP COLUMN specialist_id CASCADE");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + indexName
                + " ON " + tableName + " (specialist_profile_id, " + relatedColumn + ")");
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }
}
