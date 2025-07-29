CREATE TABLE index_item(
    id UUID NOT NULL PRIMARY KEY,
    oid VARCHAR(255) NOT NULL,
    delete BOOLEAN NOT NULL DEFAULT FALSE,
    index_type VARCHAR(255) NOT NULL,
    index_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL default '{}',
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_index_item_oid ON index_item(oid);
CREATE INDEX idx_index_item_updated ON index_item(updated);