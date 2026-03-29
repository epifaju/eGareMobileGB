-- Schéma initial aligné sur les entités JPA (PostgreSQL).
-- Bases déjà peuplées sans Flyway : baseline-on-migrate (v. application.yml) évite de rejouer ce script.

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(24) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE stations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    city VARCHAR(120),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    description VARCHAR(500),
    archived BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL REFERENCES stations (id),
    registration_code VARCHAR(32) NOT NULL,
    route_label VARCHAR(200) NOT NULL,
    capacity INTEGER NOT NULL,
    occupied_seats INTEGER NOT NULL,
    seat_layout VARCHAR(8) NOT NULL,
    status VARCHAR(32) NOT NULL,
    departure_scheduled_at TIMESTAMP(6),
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION,
    location_updated_at TIMESTAMP(6),
    fare_amount_xof INTEGER,
    archived BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_vehicles_station_id ON vehicles (station_id);
CREATE INDEX idx_vehicles_status ON vehicles (status);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles (id),
    created_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(24) NOT NULL,
    seat_number INTEGER,
    qr_token VARCHAR(2048),
    expires_at TIMESTAMP(6),
    boarding_validated_at TIMESTAMP(6),
    confirmed_at TIMESTAMP(6),
    departure_reminder_pct80_sent_at TIMESTAMP(6),
    departure_reminder_imminent_sent_at TIMESTAMP(6)
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
CREATE INDEX idx_bookings_vehicle_id ON bookings (vehicle_id);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings (id),
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(24) NOT NULL,
    provider_ref VARCHAR(128),
    status VARCHAR(16) NOT NULL,
    idempotency_key VARCHAR(128) UNIQUE,
    checkout_url_cache TEXT,
    payment_token_cache TEXT,
    refund_amount NUMERIC(12, 2),
    refunded_at TIMESTAMP(6),
    refund_provider_ref VARCHAR(128)
);

CREATE TABLE refund_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(48) NOT NULL,
    detail VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE user_push_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    expo_push_token VARCHAR(512) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_user_push_tokens_user_expo UNIQUE (user_id, expo_push_token)
);

CREATE TABLE vehicle_wait_observations (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    fill_bucket INTEGER NOT NULL,
    hour_bucket INTEGER NOT NULL,
    day_of_week INTEGER NOT NULL,
    observed_wait_minutes INTEGER NOT NULL,
    observed_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE admin_audit_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT,
    details_json TEXT
);
