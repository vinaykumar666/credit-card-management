-- Used by docker-compose on first Postgres boot.
-- For native local setup you can also run: psql -U postgres -f infra/postgres/init-databases.sql
CREATE DATABASE auth_db;
CREATE DATABASE account_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;
