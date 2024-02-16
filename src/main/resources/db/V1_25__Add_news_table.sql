CREATE TABLE news_v1
(
    id         UUID PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    title      VARCHAR(512) NOT NULL,
    text       TEXT         NOT NULL,
    status     VARCHAR(32)  NOT NULL,
    published  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(32)  NOT NULL,
    updated_by VARCHAR(32)  NOT NULL,
    author     VARCHAR(255) NOT NULL,
    UNIQUE (identifier)
);

CREATE INDEX news_v1_status_idx ON news_v1 (status);
CREATE INDEX news_v1_updated_idx ON news_v1 (updated);
