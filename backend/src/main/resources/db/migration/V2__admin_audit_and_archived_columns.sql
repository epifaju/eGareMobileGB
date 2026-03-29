-- Bases deja presentes avant Flyway : baseline v1 sans execution de V1.
-- Ce script aligne le schema sur les entites Phase 7 (audit admin + soft-delete).
-- Idempotent : safe si V1 a deja tout cree (nouvelle install).

CREATE TABLE IF NOT EXISTS admin_audit_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT,
    details_json TEXT
);

ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE stations ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;
