ALTER TABLE product_v1 ADD COLUMN agreement_id UUID;
CREATE INDEX product_v1_agreement_id_idx ON product_v1(agreement_id);
