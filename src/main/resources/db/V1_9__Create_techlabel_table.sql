CREATE TABLE IF NOT EXISTS techlabel_v1 (
    id UUID NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    guide VARCHAR(1024) NOT NULL,
    isocode VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    unit VARCHAR(64),
    created_by VARCHAR(32) NOT NULL,
    updated_by VARCHAR(32) NOT NULL,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (label),
    UNIQUE (identifier)
);
