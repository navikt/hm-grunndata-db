CREATE TABLE IF NOT EXISTS series_v1 (
    id UUID NOT NULL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    supplier_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name),
    UNIQUE (identifier)
);
