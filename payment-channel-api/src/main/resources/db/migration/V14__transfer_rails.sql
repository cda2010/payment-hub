-- Rails de transfert (LOCAL, INTRA_GROUP, STAR_UEMOA, RTGS, SWIFT)
-- + kind/groupe sur fournisseurs pour l'éligibilité

ALTER TABLE transfer_participant
    ADD COLUMN IF NOT EXISTS participant_kind VARCHAR(20) NOT NULL DEFAULT 'WALLET',
    ADD COLUMN IF NOT EXISTS group_code VARCHAR(50);

CREATE TABLE IF NOT EXISTS transfer_rail (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    rail_scope VARCHAR(20) NOT NULL DEFAULT 'EXTERNAL',
    currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    min_amount NUMERIC(18, 2),
    max_amount NUMERIC(18, 2),
    requires_bank BOOLEAN NOT NULL DEFAULT FALSE,
    same_country_only BOOLEAN NOT NULL DEFAULT FALSE,
    same_group_only BOOLEAN NOT NULL DEFAULT FALSE,
    cross_border_only BOOLEAN NOT NULL DEFAULT FALSE,
    extra_fields_json TEXT,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    ordre INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transfer_rail_actif ON transfer_rail (actif, ordre);

ALTER TABLE transfer_fee_policy
    ADD COLUMN IF NOT EXISTS rail_code VARCHAR(40);

CREATE INDEX IF NOT EXISTS idx_transfer_fee_policy_rail ON transfer_fee_policy (rail_code);

ALTER TABLE transfer_order
    ADD COLUMN IF NOT EXISTS rail_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS rail_name VARCHAR(150);
