CREATE TABLE IF NOT EXISTS assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tag VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    unit_id UUID NOT NULL REFERENCES operational_units(id),
    sector_id UUID REFERENCES sectors(id),
    main_cost_center_id UUID REFERENCES cost_centers(id),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_assets_unit ON assets(unit_id);
CREATE INDEX IF NOT EXISTS idx_assets_type ON assets(type);
