-- Execute como superuser do PostgreSQL:
-- sudo -u postgres psql -f postgres-setup.sql

CREATE DATABASE ucsal_cms;
CREATE USER cms_user WITH ENCRYPTED PASSWORD 'troque_esta_senha';
GRANT ALL PRIVILEGES ON DATABASE ucsal_cms TO cms_user;

\c ucsal_cms

-- Permissões no schema public (PostgreSQL 15+)
GRANT ALL ON SCHEMA public TO cms_user;
