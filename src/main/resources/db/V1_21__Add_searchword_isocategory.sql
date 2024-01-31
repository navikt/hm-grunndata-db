ALTER TABLE isocategory_v1 ADD COLUMN id uuid NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE isocategory_v1 DROP CONSTRAINT isocategory_v1_pkey;
ALTER TABLE isocategory_v1 ADD PRIMARY KEY (id);
ALTER TABLE isocategory_v1 ADD UNIQUE (iso_code);
ALTER TABLE isocategory_v1 ADD COLUMN search_words jsonb NOT NULL DEFAULT '[]'::jsonb;