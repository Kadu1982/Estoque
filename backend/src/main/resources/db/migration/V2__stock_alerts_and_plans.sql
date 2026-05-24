ALTER TABLE warehouses
    ADD COLUMN IF NOT EXISTS type VARCHAR(30) NOT NULL DEFAULT 'DESCENTRALIZADO';

CREATE TABLE IF NOT EXISTS warehouse_item_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    item_id UUID NOT NULL REFERENCES items(id),
    planned_quantity NUMERIC(15,3) NOT NULL,
    min_level NUMERIC(15,3),
    safety_level NUMERIC(15,3),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ,
    UNIQUE (warehouse_id, item_id)
);

CREATE TABLE IF NOT EXISTS stock_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    item_id UUID NOT NULL REFERENCES items(id),
    alert_type VARCHAR(50) NOT NULL,
    status_color VARCHAR(20) NOT NULL,
    alert_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    quantity_on_hand NUMERIC(15,3) NOT NULL,
    planned_quantity NUMERIC(15,3) NOT NULL,
    percentage_of_planned NUMERIC(7,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_stock_alerts_status
    ON stock_alerts(status_color, alert_status, created_at);

CREATE INDEX IF NOT EXISTS idx_stock_alerts_warehouse_item
    ON stock_alerts(warehouse_id, item_id, created_at);
