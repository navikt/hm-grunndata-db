ALTER TABLE techlabel_v1 DROP COLUMN options;
ALTER TABLE techlabel_v1 ADD COLUMN options jsonb DEFAULT '[]'::jsonb;