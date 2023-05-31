ALTER TABLE product_v1 ADD COLUMN agreements JSONB;
CREATE INDEX product_v1_agreements_gin_idx ON product_v1 USING GIN (agreements);
