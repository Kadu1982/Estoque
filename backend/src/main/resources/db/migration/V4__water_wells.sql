CREATE TABLE IF NOT EXISTS water_wells (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    unit_id UUID NOT NULL REFERENCES operational_units(id),
    community_name VARCHAR(255) NOT NULL,
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    population_served INTEGER,
    capacity_m3_per_day NUMERIC(12,3),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_water_wells_unit ON water_wells(unit_id);
CREATE INDEX IF NOT EXISTS idx_water_wells_community ON water_wells(community_name);
