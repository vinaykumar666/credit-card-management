-- Idempotent local DB bootstrap
--   psql -U postgres -d postgres -f infra/postgres/init-databases.sql
-- Credentials used everywhere: user=postgres  password=admin

SELECT 'CREATE DATABASE auth_db OWNER postgres'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db')\gexec

SELECT 'CREATE DATABASE account_db OWNER postgres'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'account_db')\gexec

SELECT 'CREATE DATABASE payment_db OWNER postgres'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_db')\gexec

SELECT 'CREATE DATABASE notification_db OWNER postgres'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec
