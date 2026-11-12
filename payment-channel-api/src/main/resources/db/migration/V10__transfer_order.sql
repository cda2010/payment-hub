-- Commandes de transfert (quote verrouillé → exécution)

CREATE TABLE IF NOT EXISTS transfer_order (
    id UUID PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    corridor_id UUID NOT NULL REFERENCES transfer_corridor (id),
    corridor_code VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    country_code VARCHAR(5) NOT NULL DEFAULT 'BF',
    currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    source_provider_code VARCHAR(50) NOT NULL,
    dest_provider_code VARCHAR(50) NOT NULL,
    same_provider BOOLEAN NOT NULL DEFAULT FALSE,
    principal NUMERIC(18, 2) NOT NULL,
    fee_amount NUMERIC(18, 2) NOT NULL,
    total_debit NUMERIC(18, 2) NOT NULL,
    amount_received NUMERIC(18, 2) NOT NULL,
    fee_bearer VARCHAR(20) NOT NULL DEFAULT 'SENDER',
    fee_rule_code VARCHAR(50),
    fee_breakdown TEXT,
    emitter_data_json TEXT NOT NULL,
    recipient_data_json TEXT NOT NULL,
    customer_reference VARCHAR(80),
    payment_id UUID,
    debit_ref VARCHAR(100),
    credit_ref VARCHAR(100),
    quoted_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,
    settled_at TIMESTAMPTZ,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transfer_order_status ON transfer_order (status);
CREATE INDEX IF NOT EXISTS idx_transfer_order_corridor ON transfer_order (corridor_code);
CREATE INDEX IF NOT EXISTS idx_transfer_order_expires ON transfer_order (expires_at) WHERE status = 'QUOTED';
