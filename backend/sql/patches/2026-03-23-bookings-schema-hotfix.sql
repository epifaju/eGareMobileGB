-- Hotfix schéma Postgres pour aligner la DB locale avec les dernières évolutions backend.
-- Idempotent: peut être rejoué sans effet secondaire.

-- Vehicles: layout sièges + tracking position + tarif
ALTER TABLE IF EXISTS vehicles
  ADD COLUMN IF NOT EXISTS seat_layout VARCHAR(8);

UPDATE vehicles
SET seat_layout = CASE capacity
  WHEN 8 THEN 'L8'
  WHEN 15 THEN 'L15'
  WHEN 20 THEN 'L20'
  WHEN 45 THEN 'L45'
  ELSE seat_layout
END
WHERE capacity IN (8, 15, 20, 45);

UPDATE vehicles
SET seat_layout = 'L20'
WHERE seat_layout IS NULL;

ALTER TABLE IF EXISTS vehicles
  ALTER COLUMN seat_layout SET DEFAULT 'L20';

ALTER TABLE IF EXISTS vehicles
  ALTER COLUMN seat_layout SET NOT NULL;

ALTER TABLE IF EXISTS vehicles
  ADD COLUMN IF NOT EXISTS fare_amount_xof INTEGER;

ALTER TABLE IF EXISTS vehicles
  ADD COLUMN IF NOT EXISTS current_latitude DOUBLE PRECISION;

ALTER TABLE IF EXISTS vehicles
  ADD COLUMN IF NOT EXISTS current_longitude DOUBLE PRECISION;

ALTER TABLE IF EXISTS vehicles
  ADD COLUMN IF NOT EXISTS location_updated_at TIMESTAMP WITHOUT TIME ZONE;

-- Bookings: colonnes utilisées par le domaine booking
ALTER TABLE IF EXISTS bookings
  ADD COLUMN IF NOT EXISTS seat_number INTEGER;

ALTER TABLE IF EXISTS bookings
  ADD COLUMN IF NOT EXISTS qr_token VARCHAR(64);

ALTER TABLE IF EXISTS bookings
  ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITHOUT TIME ZONE;

-- Paiements (si absents localement)
CREATE TABLE IF NOT EXISTS payments (
  id BIGSERIAL PRIMARY KEY,
  booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
  amount NUMERIC(12,2) NOT NULL DEFAULT 0,
  currency VARCHAR(3) NOT NULL DEFAULT 'XOF',
  provider VARCHAR(24) NOT NULL DEFAULT 'INTERNAL',
  provider_ref VARCHAR(128),
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
);

-- Observations attente (R4 v2)
CREATE TABLE IF NOT EXISTS vehicle_wait_observations (
  id BIGSERIAL PRIMARY KEY,
  station_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  fill_bucket INTEGER NOT NULL,
  hour_bucket INTEGER NOT NULL,
  day_of_week INTEGER NOT NULL,
  observed_wait_minutes INTEGER NOT NULL,
  observed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_wait_obs_station_status_time
  ON vehicle_wait_observations (station_id, status, observed_at DESC);
