CREATE TABLE IF NOT EXISTS water_sampling_points (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    well_id UUID NOT NULL REFERENCES water_wells(id),
    name VARCHAR(255) NOT NULL,
    parameters TEXT,
    frequency_days INTEGER,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS water_samples (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sampling_point_id UUID NOT NULL REFERENCES water_sampling_points(id),
    sample_code VARCHAR(100) NOT NULL UNIQUE,
    classification VARCHAR(30) NOT NULL,
    collected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_water_sampling_points_well ON water_sampling_points(well_id);
CREATE INDEX IF NOT EXISTS idx_water_samples_point ON water_samples(sampling_point_id);
CREATE INDEX IF NOT EXISTS idx_water_samples_classification ON water_samples(classification);
