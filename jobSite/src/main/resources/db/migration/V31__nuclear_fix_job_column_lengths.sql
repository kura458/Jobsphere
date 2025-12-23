-- Comprehensive nuclear fix for all character limit issues in jobs and addresses
-- We are converting every potential bottleneck to TEXT to ensure NO "value too long" errors can occur.

ALTER TABLE jobs 
    ALTER COLUMN title TYPE TEXT,
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN category TYPE TEXT,
    ALTER COLUMN education_level TYPE TEXT,
    ALTER COLUMN experience_level TYPE TEXT,
    ALTER COLUMN experience_description TYPE TEXT,
    ALTER COLUMN job_type TYPE TEXT,
    ALTER COLUMN workplace_type TYPE TEXT,
    ALTER COLUMN compensation_type TYPE TEXT,
    ALTER COLUMN status TYPE TEXT,
    ALTER COLUMN gender_requirement TYPE TEXT,
    ALTER COLUMN currency TYPE TEXT;

-- Address table can also be a source of overflow if the user inputs long regions or street names
ALTER TABLE addresses 
    ALTER COLUMN country TYPE TEXT,
    ALTER COLUMN region TYPE TEXT,
    ALTER COLUMN city TYPE TEXT,
    ALTER COLUMN sub_city TYPE TEXT,
    ALTER COLUMN street TYPE TEXT;

-- Comments to verify
COMMENT ON TABLE jobs IS 'Job postings with unconstrained text lengths for all descriptive fields';
