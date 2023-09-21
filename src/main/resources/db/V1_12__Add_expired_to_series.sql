ALTER TABLE series_v1 drop constraint series_v1_name_key;
ALTER TABLE series_v1 ADD COLUMN expired TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;