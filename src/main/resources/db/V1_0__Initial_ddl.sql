CREATE SEQUENCE IF NOT EXISTS hmdbleverandorbatch_v1_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS hmdbleverandorbatch_v1 (
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('hmdbleverandorbatch_v1_id_seq'),
    leverandorer JSONB NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id)
);

CREATE SEQUENCE IF NOT EXISTS hmdbproduktbatch_v1_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS hmdbproduktbatch_v1 (
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('hmdbproduktbatch_v1_id_seq'),
    produkter JSONB NOT NULL,
    tekniske_data JSONB NOT NUll,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id)
);

CREATE SEQUENCE IF NOT EXISTS supplier_v1_id_seq START WITH 1000000;

CREATE TABLE IF NOT EXISTS supplier_v1 (
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('supplier_v1_id_seq'),
    hmdb_id NUMERIC(19,0),
    uuid VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    info JSONB NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (uuid),
    UNIQUE (name)
);

CREATE SEQUENCE IF NOT EXISTS product_v1_id_seq START WITH 1000;

CREATE TABLE IF NOT EXISTS product_v1 (
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('product_v1_id_seq'),
    uuid VARCHAR(36) NOT NULL,
    supplier_id NUMERIC(19,0) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description JSONB NOT NULL,
    hms_artnr VARCHAR(255),
    hmdb_artid VARCHAR(255),
    supplier_ref VARCHAR(255) NOT NULL,
    iso_category VARCHAR(255) NOT NULL,
    accessory BOOLEAN NOT NULL DEFAULT FALSE,
    part BOOLEAN NOT NULL DEFAULT FALSE,
    series_id VARCHAR(255),
    data JSONB NOT NULL,
    media JSONB NOT NULL,
    agreement JSONB,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (uuid),
    UNIQUE (supplier_id, supplier_ref),
    CONSTRAINT fk_supplier_product_v1_log FOREIGN KEY (supplier_id) REFERENCES supplier_v1(id)
)

