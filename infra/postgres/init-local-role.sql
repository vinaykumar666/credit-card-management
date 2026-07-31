-- Optional for native (no Docker) local setup.
-- Run after init-databases.sql if you want user/password cards/cards:
--   psql -U postgres -f infra/postgres/init-local-role.sql

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cards') THEN
    CREATE USER cards WITH PASSWORD 'cards';
  END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO cards;
GRANT ALL PRIVILEGES ON DATABASE account_db TO cards;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO cards;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO cards;
