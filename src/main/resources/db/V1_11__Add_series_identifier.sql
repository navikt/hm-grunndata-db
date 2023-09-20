ALTER TABLE product_v1 ADD COLUMN series_identifier VARCHAR(255);
UPDATE product_v1 SET series_identifier = series_id;
