CREATE TABLE IF NOT EXISTS service_job_v1 (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    supplier_id UUID NOT NULL,
    supplier_ref VARCHAR(255),
    hms_art_nr VARCHAR(64) NOT NULL,
    iso_category VARCHAR(64) NOT NULL,
    published TIMESTAMP NOT NULL,
    expired TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    attributes JSONB NOT NULL,
    agreements JSONB NOT NULL
);