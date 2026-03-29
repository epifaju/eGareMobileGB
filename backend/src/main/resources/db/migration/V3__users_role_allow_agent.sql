-- Rôle AGENT : étendre la contrainte CHECK sur users.role (bases où elle existe déjà sans AGENT).
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'AGENT', 'DRIVER', 'ADMIN'));
