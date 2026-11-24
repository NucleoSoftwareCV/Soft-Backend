-- Apply after rewards-readonly.sql. Preserve existing rules and select types by slug.
BEGIN;

INSERT INTO earn_points_rules (experience_type_id, reward, description, active)
SELECT et.id, rule.reward, 'Asistencia a ' || et.name, true
FROM (VALUES ('retiros', 10), ('talleres', 20), ('ceremonias', 25),
             ('encuentros-grupales', 30), ('formaciones', 35), ('clases', 45)) AS rule(slug, reward)
JOIN experience_types et ON et.slug = rule.slug
WHERE NOT EXISTS (SELECT 1 FROM earn_points_rules current_rule WHERE current_rule.experience_type_id = et.id);

CREATE UNIQUE INDEX IF NOT EXISTS points_transactions_attendance_earn_idx
    ON points_transactions (event_attendee_id)
    WHERE type = 'EARN' AND event_attendee_id IS NOT NULL;

COMMIT;
