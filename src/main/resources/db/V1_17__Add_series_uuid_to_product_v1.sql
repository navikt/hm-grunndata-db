ALTER TABLE product_v1 ADD COLUMN series_uuid UUID;
CREATE INDEX product_v1_series_uuid_idx ON product_v1 (series_uuid);