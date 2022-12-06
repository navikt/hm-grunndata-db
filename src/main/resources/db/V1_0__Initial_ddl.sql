CREATE SEQUENCE IF NOT EXISTS hmdbbatch_v1_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS hmdbbatch_v1 (
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('hmdbbatch_v1_id_seq'),
    name VARCHAR(32) NOT NULL,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    syncfrom TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS supplier_v1 (
    id UUID NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    info JSONB NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (identifier)
);

CREATE TABLE IF NOT EXISTS agreement_v1 (
    id UUID NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    title VARCHAR(1024) NOT NULL,
    resume VARCHAR(1024),
    text TEXT NOT NULL,
    link VARCHAR(1024),
    reference VARCHAR(255) NOT NULL,
    publish TIMESTAMP,
    expire TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE(identifier),
    UNIQUE(reference)
);

CREATE TABLE IF NOT EXISTS agreement_post_v1 (
    id UUID NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    agreement_id UUID NOT NULL,
    nr INTEGER NOT NULL,
    title VARCHAR(1024) NOT NULL,
    description TEXT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE(identifier),
    CONSTRAINT fk_agreement_post_v1_log FOREIGN KEY (agreement_id) REFERENCES agreement_v1(id)
);

CREATE SEQUENCE IF NOT EXISTS product_v1_id_seq START WITH 1000;

CREATE TABLE IF NOT EXISTS product_v1 (
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('product_v1_id_seq'),
    uuid VARCHAR(36) NOT NULL,
    supplier_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description JSONB NOT NULL,
    hms_artnr VARCHAR(255),
    identifier VARCHAR(255) NOT NULL,
    supplier_ref VARCHAR(255) NOT NULL,
    iso_category VARCHAR(255) NOT NULL,
    accessory BOOLEAN NOT NULL DEFAULT FALSE,
    sparepart BOOLEAN NOT NULL DEFAULT FALSE,
    series_id VARCHAR(255),
    tech_data JSONB NOT NULL,
    media JSONB NOT NULL,
    agreement JSONB,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (uuid),
    UNIQUE (identifier),
    UNIQUE (supplier_id, supplier_ref),
    CONSTRAINT fk_supplier_product_v1_log FOREIGN KEY (supplier_id) REFERENCES supplier_v1(id)
)

