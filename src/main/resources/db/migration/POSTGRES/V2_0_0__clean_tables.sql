DROP TABLE IF EXISTS service CASCADE;

-- Table: service
CREATE TABLE service (
    id varchar(255) NOT NULL PRIMARY KEY,
    default_oidc_scope varchar(255)
);

-- Table: scope_entry
CREATE TABLE scope_entry (
    id BIGSERIAL PRIMARY KEY,
    service_id VARCHAR(255) REFERENCES service (id) ON DELETE CASCADE,
    scope_key VARCHAR(255),
    credentials TEXT NOT NULL,
    presentation_definition TEXT NOT NULL
);
