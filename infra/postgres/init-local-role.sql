-- Ensure local superuser password is admin (native Windows Postgres).
--   psql -U postgres -d postgres -f infra/postgres/init-local-role.sql
-- (May require a temporary trust entry in pg_hba.conf if the current password is unknown.)

ALTER ROLE postgres WITH LOGIN PASSWORD 'admin';
