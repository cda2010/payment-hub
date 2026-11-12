-- Lot 2 : droits de commission accrues + liquidations.

CREATE TABLE commission_droit (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64),
    transaction_id      VARCHAR(64)  NOT NULL,
    grille_id           UUID,
    grille_code         VARCHAR(64),
    grille_version      INT,
    rule_id             UUID,
    rule_code           VARCHAR(64),
    beneficiary_role    VARCHAR(64)  NOT NULL,
    beneficiary_ref     VARCHAR(120),
    currency            VARCHAR(3)   NOT NULL DEFAULT 'XOF',
    amount              NUMERIC(19, 4) NOT NULL,
    funding_type        VARCHAR(32),
    funder_role         VARCHAR(64),
    status              VARCHAR(32)  NOT NULL DEFAULT 'ACCRUED',
    liquidation_id      UUID,
    detail              VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    liquidated_at       TIMESTAMPTZ
);

CREATE INDEX idx_commission_droit_tx ON commission_droit (transaction_id);
CREATE INDEX idx_commission_droit_status ON commission_droit (status, beneficiary_role);
CREATE UNIQUE INDEX uq_commission_droit_tx_rule ON commission_droit (transaction_id, rule_code);

CREATE TABLE commission_liquidation (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64),
    beneficiary_role    VARCHAR(64)  NOT NULL,
    beneficiary_ref     VARCHAR(120),
    currency            VARCHAR(3)   NOT NULL DEFAULT 'XOF',
    total_amount        NUMERIC(19, 4) NOT NULL,
    droit_count         INT NOT NULL DEFAULT 0,
    status              VARCHAR(32)  NOT NULL DEFAULT 'COMPLETED',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
