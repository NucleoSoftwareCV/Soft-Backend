package com.hean.consigueventas.oonabe.experienceType.config;

import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class ExperienceTypeSeeder {
    private static final Map<String, String> LEGACY_TYPES = Map.of(
            "TALLER", "Talleres",
            "RETIRO", "Retiros",
            "CLASE", "Clases",
            "CEREMONIA", "Ceremonias",
            "ENCUENTRO_GRUPAL", "Encuentros Grupales",
            "FORMACION", "Formaciones");

    private final ExperienceTypeRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public ExperienceTypeSeeder(ExperienceTypeRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void seed() {
        LEGACY_TYPES.values().forEach(this::seedType);
        migrateLegacyEventValues();
    }

    private void seedType(String name) {
        String slug = slugFor(name);
        ExperienceType type = repository.findBySlug(slug).orElse(null);
        if (type == null) {
            type = new ExperienceType();
            type.setName(name);
            type.setDescription("Eventos clasificados como " + name.toLowerCase() + ".");
            type.setActive(true);
            repository.saveAndFlush(type);
        }
    }

    private void migrateLegacyEventValues() {
        if (!columnExists("events", "event_type") || !columnExists("events", "experience_type_id")) {
            return;
        }
        LEGACY_TYPES.forEach((legacyValue, name) -> jdbcTemplate.update(
                "UPDATE events SET experience_type_id = (SELECT id FROM experience_types WHERE slug = ?) "
                        + "WHERE experience_type_id IS NULL AND event_type = ?",
                slugFor(name), legacyValue));
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }

    private String slugFor(String name) {
        return name.toLowerCase().replace(" ", "-");
    }
}
