-- Corridors de transfert local + grilles de frais

CREATE TABLE IF NOT EXISTS fee_rule (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    description TEXT,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fee_rule_line (
    id UUID PRIMARY KEY,
    fee_rule_id UUID NOT NULL REFERENCES fee_rule (id) ON DELETE CASCADE,
    fee_type VARCHAR(20) NOT NULL,
    min_amount NUMERIC(18, 2),
    max_amount NUMERIC(18, 2),
    value NUMERIC(18, 4) NOT NULL,
    ordre INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_fee_rule_line_rule ON fee_rule_line (fee_rule_id);

CREATE TABLE IF NOT EXISTS transfer_corridor (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    country_code VARCHAR(5) NOT NULL DEFAULT 'BF',
    currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    source_provider_code VARCHAR(50) NOT NULL,
    dest_provider_code VARCHAR(50) NOT NULL,
    same_provider BOOLEAN NOT NULL DEFAULT FALSE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    min_amount NUMERIC(18, 2),
    max_amount NUMERIC(18, 2),
    source_fields_json TEXT,
    dest_fields_json TEXT,
    fee_rule_id UUID REFERENCES fee_rule (id),
    ordre_affichage INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transfer_corridor_country ON transfer_corridor (country_code, actif);
CREATE INDEX IF NOT EXISTS idx_transfer_corridor_rails ON transfer_corridor (source_provider_code, dest_provider_code);
