-- Facturiers et produits (parcours + politique de montant)

CREATE TABLE IF NOT EXISTS biller (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    default_currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    ordre_affichage INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS biller_product (
    id UUID PRIMARY KEY,
    biller_id UUID NOT NULL REFERENCES biller (id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    lookup_mode VARCHAR(30) NOT NULL DEFAULT 'BOTH',
    amount_policy VARCHAR(30) NOT NULL DEFAULT 'FULL_ONLY',
    min_amount NUMERIC(18, 2),
    max_amount NUMERIC(18, 2),
    allow_overpay BOOLEAN NOT NULL DEFAULT FALSE,
    journey_code VARCHAR(50) NOT NULL DEFAULT 'OPEN_BILLS',
    payment_flow_id UUID,
    ordre_affichage INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_biller_product_code UNIQUE (biller_id, code)
);

CREATE INDEX IF NOT EXISTS idx_biller_actif ON biller (actif);
CREATE INDEX IF NOT EXISTS idx_biller_product_biller ON biller_product (biller_id);
CREATE INDEX IF NOT EXISTS idx_biller_product_actif ON biller_product (actif);
