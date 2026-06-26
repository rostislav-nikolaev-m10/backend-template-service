DROP SCHEMA IF EXISTS "demo" CASCADE;
CREATE SCHEMA IF NOT EXISTS "demo";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE demo.user_profile
(
    id              uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    username        VARCHAR     NOT NULL,
    first_name      VARCHAR,
    first_name_hash VARCHAR,
    last_name       VARCHAR,
    last_name_hash  VARCHAR,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE demo.user_profile IS 'User profile table';
COMMENT ON COLUMN demo.user_profile.id IS 'User profile surrogate key';
