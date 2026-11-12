-- Fournisseurs : capacités + politiques de frais (same / cross / override rare)

ALTER TABLE transfer_participant
    ADD COLUMN IF NOT EXISTS can_emit BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS can_receive BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS min_amount NUMERIC(18, 2),
    ADD COLUMN IF NOT EXISTS max_amount NUMERIC(18, 2);

CREATE TABLE IF NOT EXISTS transfer_fee_policy (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    policy_type VARCHAR(30) NOT NULL,
    country_code VARCHAR(5) NOT NULL DEFAULT 'BF',
    currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    emitter_code VARCHAR(50),
    beneficiary_code VARCHAR(50),
    fee_rule_id UUID NOT NULL REFERENCES fee_rule (id),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    ordre INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transfer_fee_policy_type ON transfer_fee_policy (policy_type, country_code, actif);
CREATE INDEX IF NOT EXISTS idx_transfer_fee_policy_pair ON transfer_fee_policy (emitter_code, beneficiary_code);

-- Ordres de transfert : corridor optionnel (legacy), fournisseurs obligatoires
ALTER TABLE transfer_order
    ALTER COLUMN corridor_id DROP NOT NULL,
    ALTER COLUMN corridor_code DROP NOT NULL;

ALTER TABLE transfer_order
    ADD COLUMN IF NOT EXISTS emitter_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS beneficiary_name VARCHAR(150);
