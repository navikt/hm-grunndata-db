CREATE TABLE IF NOT EXISTS isocategory_v1 (
    iso_code VARCHAR(32) NOT NULL PRIMARY KEY,
    iso_title VARCHAR(1024) NOT NULL,
    iso_text TEXT NOT NULL,
    iso_text_short TEXT NOT NULL,
    iso_text_long TEXT NOT NULL,
    iso_translations JSONB NOT NULL,
    iso_level INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    show_tech BOOLEAN NOT NULL,
    allow_multi BOOLEAN NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL
);
