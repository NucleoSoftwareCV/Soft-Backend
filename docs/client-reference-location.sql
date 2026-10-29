-- PostgreSQL: apply during deployment when Hibernate schema update is disabled.
-- Additive and nullable: existing accounts keep all their current preferences.
ALTER TABLE client_profiles ADD COLUMN IF NOT EXISTS reference_label varchar(300);
ALTER TABLE client_profiles ADD COLUMN IF NOT EXISTS reference_latitude double precision;
ALTER TABLE client_profiles ADD COLUMN IF NOT EXISTS reference_longitude double precision;
ALTER TABLE client_profiles ADD COLUMN IF NOT EXISTS reference_approximate boolean;
