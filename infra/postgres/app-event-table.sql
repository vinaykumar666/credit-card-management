-- Shared footfall / lifecycle event table (also applied via Flyway in each service DB).
-- Tracks every meaningful step into and through the platform.

CREATE TABLE IF NOT EXISTS app_event (
  id UUID PRIMARY KEY,
  event_name VARCHAR(100) NOT NULL,
  event_phase VARCHAR(20) NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  method_name VARCHAR(150),
  user_id UUID,
  user_name VARCHAR(255),
  amount NUMERIC(19, 4),
  transaction_id VARCHAR(64),
  correlation_id VARCHAR(64),
  channel_id VARCHAR(64),
  client_id VARCHAR(64),
  http_method VARCHAR(16),
  path VARCHAR(512),
  status VARCHAR(40),
  duration_ms BIGINT,
  details VARCHAR(2000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_app_event_created_at ON app_event (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_app_event_user_id ON app_event (user_id);
CREATE INDEX IF NOT EXISTS idx_app_event_name ON app_event (event_name);
CREATE INDEX IF NOT EXISTS idx_app_event_correlation ON app_event (correlation_id);
