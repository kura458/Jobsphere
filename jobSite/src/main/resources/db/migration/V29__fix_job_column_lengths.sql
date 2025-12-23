-- Increase column lengths for jobs table to prevent "value too long" errors
-- We are converting critical fields to TEXT to avoid any length limits

ALTER TABLE jobs 
    ALTER COLUMN title TYPE TEXT,
    ALTER COLUMN category TYPE TEXT,
    ALTER COLUMN education_level TYPE TEXT,
    ALTER COLUMN experience_level TYPE TEXT,
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN experience_description TYPE TEXT;

-- Expand other VARCHAR columns
ALTER TABLE jobs 
    ALTER COLUMN job_type TYPE VARCHAR(255),
    ALTER COLUMN workplace_type TYPE VARCHAR(255),
    ALTER COLUMN compensation_type TYPE VARCHAR(255),
    ALTER COLUMN status TYPE VARCHAR(255),
    ALTER COLUMN gender_requirement TYPE VARCHAR(255);

-- Ensure addresses table columns are also expanded if needed
ALTER TABLE addresses 
    ALTER COLUMN country TYPE VARCHAR(255),
    ALTER COLUMN region TYPE VARCHAR(255),
    ALTER COLUMN city TYPE VARCHAR(255),
    ALTER COLUMN sub_city TYPE VARCHAR(255),
    ALTER COLUMN street TYPE TEXT;
