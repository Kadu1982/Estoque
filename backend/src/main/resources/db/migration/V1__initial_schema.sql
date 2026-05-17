-- ================================================================
-- Austral Estoque — Schema inicial
-- Flyway V1__initial_schema.sql
-- ================================================================

-- Extensões
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ── ORGANIZAÇÃO HIERÁRQUICA ──────────────────────────────────────

CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    document VARCHAR(50),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE operational_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID NOT NULL REFERENCES countries(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    type VARCHAR(50) NOT NULL, -- HOSPITAL, CANTEIRO, OFICINA, ADMINISTRATIVO, BASE_REGIONAL
    address TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id UUID NOT NULL REFERENCES operational_units(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE sectors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id UUID NOT NULL REFERENCES operational_units(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE cost_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sector_id UUID REFERENCES sectors(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    nature VARCHAR(100),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

-- ── USUÁRIOS & RBAC ─────────────────────────────────────────────

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

INSERT INTO roles (name, description) VALUES
    ('ADMIN',         'Administrador do sistema'),
    ('DIRETOR',       'Diretor / Aprovador final'),
    ('FINANCEIRO',    'Financeiro / Controladoria'),
    ('COMPRADOR',     'Comprador'),
    ('ALMOXARIFE',    'Almoxarife'),
    ('GESTOR_SETOR',  'Gestor do setor'),
    ('SOLICITANTE',   'Solicitante');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    whatsapp VARCHAR(50),
    auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    external_id VARCHAR(255),
    unit_id UUID REFERENCES operational_units(id),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── ITENS & CATEGORIAS ──────────────────────────────────────────

CREATE TABLE item_families (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE item_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES item_families(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    category_id UUID REFERENCES item_categories(id),
    unit_of_measure VARCHAR(50) NOT NULL,
    brand VARCHAR(100),
    specification TEXT,
    criticality VARCHAR(20) NOT NULL DEFAULT 'MEDIO',
    min_stock NUMERIC(15,3) DEFAULT 0,
    max_stock NUMERIC(15,3),
    lead_time_days INTEGER,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

-- ── FORNECEDORES ────────────────────────────────────────────────

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50), -- LOCAL, IMPORTADO
    country VARCHAR(100),
    currency VARCHAR(10) DEFAULT 'USD',
    payment_term_days INTEGER,
    contact_name VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    contact_whatsapp VARCHAR(50),
    notes TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE item_suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id UUID NOT NULL REFERENCES items(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    is_homologated BOOLEAN DEFAULT false,
    unit_price_usd NUMERIC(15,2),
    lead_time_days INTEGER,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (item_id, supplier_id)
);

-- ── ALÇADAS DE APROVAÇÃO ────────────────────────────────────────

CREATE TABLE approval_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    min_amount_usd NUMERIC(15,2) NOT NULL DEFAULT 0,
    max_amount_usd NUMERIC(15,2),          -- NULL = sem limite
    approver_role VARCHAR(50) NOT NULL,
    item_criticality VARCHAR(20),          -- NULL = qualquer
    special_rule VARCHAR(50),              -- CAPEX, EMERGENCIAL, FORA_CONTRATO
    sla_hours INTEGER NOT NULL DEFAULT 24,
    escalation_after_hours INTEGER DEFAULT 48,
    order_index INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

-- Alçadas padrão configuráveis (valores em USD, ajustar via UI)
INSERT INTO approval_tiers (name, min_amount_usd, max_amount_usd, approver_role, sla_hours, escalation_after_hours, order_index) VALUES
    ('Gestor do Setor',    0,       500,   'GESTOR_SETOR',  24, 48, 1),
    ('Gerente da Unidade', 500.01,  5000,  'FINANCEIRO',    48, 72, 2),
    ('Diretoria Regional', 5000.01, 50000, 'DIRETOR',       72, 96, 3),
    ('Diretor Executivo',  50000.01, NULL, 'ADMIN',         96, 120, 4);

-- ── REQUISIÇÕES ─────────────────────────────────────────────────

CREATE TABLE requisitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    requester_id UUID NOT NULL REFERENCES users(id),
    unit_id UUID NOT NULL REFERENCES operational_units(id),
    warehouse_id UUID REFERENCES warehouses(id),
    sector_id UUID REFERENCES sectors(id),
    cost_center_id UUID NOT NULL REFERENCES cost_centers(id),
    status VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO',
    urgency VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    justification TEXT,
    notes TEXT,
    estimated_total_usd NUMERIC(15,2),
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE requisition_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requisition_id UUID NOT NULL REFERENCES requisitions(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES items(id),
    quantity NUMERIC(15,3) NOT NULL,
    unit_of_measure VARCHAR(50),
    estimated_unit_price_usd NUMERIC(15,2),
    notes TEXT,
    status VARCHAR(50) DEFAULT 'PENDENTE',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── APROVAÇÕES ──────────────────────────────────────────────────

CREATE TABLE approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requisition_id UUID NOT NULL REFERENCES requisitions(id),
    tier_id UUID NOT NULL REFERENCES approval_tiers(id),
    approver_id UUID REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    decision_at TIMESTAMPTZ,
    justification TEXT,
    sla_deadline TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(), updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── COTAÇÕES ────────────────────────────────────────────────────

CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requisition_id UUID NOT NULL REFERENCES requisitions(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    buyer_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTA',
    response_deadline DATE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE quotation_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quotation_id UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    requisition_item_id UUID NOT NULL REFERENCES requisition_items(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    unit_price_usd NUMERIC(15,2),
    total_price_usd NUMERIC(15,2),
    lead_time_days INTEGER,
    payment_condition VARCHAR(100),
    availability VARCHAR(50),
    is_winner BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── PEDIDOS DE COMPRA ───────────────────────────────────────────

CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    requisition_id UUID NOT NULL REFERENCES requisitions(id),
    quotation_id UUID REFERENCES quotations(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    buyer_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTO',
    total_amount_usd NUMERIC(15,2),
    estimated_delivery_date DATE,
    is_local_supplier BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE purchase_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    po_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES items(id),
    quantity NUMERIC(15,3) NOT NULL,
    unit_price_usd NUMERIC(15,2) NOT NULL,
    total_price_usd NUMERIC(15,2) NOT NULL,
    received_quantity NUMERIC(15,3) DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── RECEBIMENTO ─────────────────────────────────────────────────

CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    po_id UUID NOT NULL REFERENCES purchase_orders(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    receipt_date DATE NOT NULL,
    invoice_number VARCHAR(100),
    notes TEXT,
    has_divergence BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW(), created_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW(), updated_by VARCHAR(100), deleted_at TIMESTAMPTZ
);

CREATE TABLE receipt_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_id UUID NOT NULL REFERENCES receipts(id) ON DELETE CASCADE,
    po_item_id UUID NOT NULL REFERENCES purchase_order_items(id),
    item_id UUID NOT NULL REFERENCES items(id),
    expected_quantity NUMERIC(15,3),
    received_quantity NUMERIC(15,3) NOT NULL,
    unit_cost_usd NUMERIC(15,2),
    divergence_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── ESTOQUE ─────────────────────────────────────────────────────

CREATE TABLE stock_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    item_id UUID NOT NULL REFERENCES items(id),
    quantity NUMERIC(15,3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(15,3) NOT NULL DEFAULT 0,
    avg_cost_usd NUMERIC(15,2),
    last_movement_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (warehouse_id, item_id)
);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    item_id UUID NOT NULL REFERENCES items(id),
    movement_type VARCHAR(50) NOT NULL,
    quantity NUMERIC(15,3) NOT NULL,
    unit_cost_usd NUMERIC(15,2),
    total_cost_usd NUMERIC(15,2),
    sector_id UUID REFERENCES sectors(id),
    cost_center_id UUID REFERENCES cost_centers(id),
    reference_type VARCHAR(50),   -- RECEIPT, REQUISITION, TRANSFER, INVENTORY, MANUAL
    reference_id UUID,
    origin_warehouse_id UUID REFERENCES warehouses(id),
    destination_warehouse_id UUID REFERENCES warehouses(id),
    movement_reason VARCHAR(255),
    notes TEXT,
    user_id UUID NOT NULL REFERENCES users(id),
    movement_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE stock_reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    item_id UUID NOT NULL REFERENCES items(id),
    requisition_item_id UUID REFERENCES requisition_items(id),
    quantity NUMERIC(15,3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    created_at TIMESTAMPTZ DEFAULT NOW(), updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── AUDITORIA ───────────────────────────────────────────────────

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100),
    entity_id UUID,
    action VARCHAR(50) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    user_id UUID,
    username VARCHAR(100),
    ip_address VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    attempts INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMPTZ,
    error_message TEXT,
    payload JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── ÍNDICES ─────────────────────────────────────────────────────

CREATE INDEX idx_requisitions_status     ON requisitions(status);
CREATE INDEX idx_requisitions_requester  ON requisitions(requester_id);
CREATE INDEX idx_requisitions_unit       ON requisitions(unit_id);
CREATE INDEX idx_approvals_status        ON approvals(status);
CREATE INDEX idx_approvals_requester     ON approvals(requisition_id);
CREATE INDEX idx_stock_balances_lookup   ON stock_balances(warehouse_id, item_id);
CREATE INDEX idx_stock_movements_item    ON stock_movements(item_id, movement_date);
CREATE INDEX idx_po_status              ON purchase_orders(status);
CREATE INDEX idx_items_code             ON items(code);
CREATE INDEX idx_items_description      ON items USING gin(description gin_trgm_ops);
CREATE INDEX idx_audit_entity           ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_notification_status    ON notification_logs(status, created_at);
