-- Versions du catalogue de services + référence transactionnelle
CREATE TABLE IF NOT EXISTS catalog_de_service_version (
    id                    UUID         PRIMARY KEY,
    catalog_de_service_id UUID         NOT NULL REFERENCES catalog_de_service(id) ON DELETE CASCADE,
    version               VARCHAR(20)  NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    effective_from        TIMESTAMPTZ,
    effective_to          TIMESTAMPTZ,
    workflow_key          VARCHAR(120),
    configuration         JSONB,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_catalog_service_version UNIQUE (catalog_de_service_id, version),
    CONSTRAINT chk_catalog_service_version_status CHECK (
        status IN ('DRAFT', 'VALIDATED', 'PUBLISHED', 'SUSPENDED', 'ARCHIVED')
    )
);

CREATE INDEX IF NOT EXISTS idx_catalog_svc_ver_service ON catalog_de_service_version (catalog_de_service_id);
CREATE INDEX IF NOT EXISTS idx_catalog_svc_ver_status ON catalog_de_service_version (status);

-- Seed v1.0 pour chaque service existant (workflow par défaut selon le code)
INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, effective_from, effective_to,
    workflow_key, configuration, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    s.id,
    '1.0',
    COALESCE(s.status, 'PUBLISHED'),
    NOW(),
    NULL,
    CASE s.code
        WHEN 'ACCOUNT_TRANSFER' THEN 'providerWalletTransfer'
        WHEN 'BILL_PAYMENT' THEN 'providerBillPay'
        WHEN 'INTERNATIONAL_TRANSFER' THEN 'providerInternationalTransfer'
        WHEN 'REMITTANCE' THEN 'providerRemittance'
        WHEN 'BANK_TO_WALLET' THEN 'providerBankToWallet'
        WHEN 'WALLET_TO_BANK' THEN 'providerWalletToBank'
        WHEN 'CASH_OUT' THEN 'providerCashOut'
        WHEN 'CASH_IN' THEN 'providerCashIn'
        ELSE NULL
    END,
    '{}'::jsonb,
    NOW(),
    NOW()
FROM catalog_de_service s
WHERE NOT EXISTS (
    SELECT 1 FROM catalog_de_service_version v
    WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
);

-- Remplacer current_version INTEGER par pointeur vers la version courante
ALTER TABLE catalog_de_service
    ADD COLUMN IF NOT EXISTS current_version_id UUID;

UPDATE catalog_de_service s
SET current_version_id = v.id
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND v.version = '1.0'
  AND s.current_version_id IS NULL;

ALTER TABLE catalog_de_service
    DROP CONSTRAINT IF EXISTS chk_catalog_de_service_version;

ALTER TABLE catalog_de_service
    DROP COLUMN IF EXISTS current_version;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_catalog_de_service_current_version'
    ) THEN
        ALTER TABLE catalog_de_service
            ADD CONSTRAINT fk_catalog_de_service_current_version
            FOREIGN KEY (current_version_id) REFERENCES catalog_de_service_version(id)
            ON DELETE SET NULL;
    END IF;
END $$;

-- Snapshot exact de la version produit utilisée par une transaction
ALTER TABLE payment_transaction
    ADD COLUMN IF NOT EXISTS catalog_service_version_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_payment_tx_catalog_service_version'
    ) THEN
        ALTER TABLE payment_transaction
            ADD CONSTRAINT fk_payment_tx_catalog_service_version
            FOREIGN KEY (catalog_service_version_id) REFERENCES catalog_de_service_version(id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_payment_tx_catalog_svc_ver
    ON payment_transaction (catalog_service_version_id);
