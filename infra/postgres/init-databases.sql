-- Idempotent local DB bootstrap (psql -U cards -d postgres -f this file)
-- Password for cards/postgres should already be admin (see start-all-local.bat).

SELECT 'CREATE DATABASE auth_db OWNER cards'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db')\gexec

SELECT 'CREATE DATABASE account_db OWNER cards'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'account_db')\gexec

SELECT 'CREATE DATABASE payment_db OWNER cards'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_db')\gexec

SELECT 'CREATE DATABASE notification_db OWNER cards'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec
