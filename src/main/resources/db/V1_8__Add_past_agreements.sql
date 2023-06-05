ALTER TABLE product_v1 ADD COLUMN past_agreements JSONB NOT NULL DEFAULT '[]'::jsonb;
CREATE INDEX product_v1_past_agreements_gin_idx ON product_v1 USING GIN (past_agreements);
