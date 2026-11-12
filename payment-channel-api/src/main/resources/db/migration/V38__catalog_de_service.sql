-- Catalogue de services produit (intention métier ≠ fournisseurs)
CREATE TABLE IF NOT EXISTS catalog_de_service (
    id               UUID         PRIMARY KEY,
    code             VARCHAR(60)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    category         VARCHAR(50)  NOT NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    current_version  INTEGER      NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_catalog_de_service_status CHECK (
        status IN ('DRAFT', 'VALIDATED', 'PUBLISHED', 'SUSPENDED', 'ARCHIVED')
    ),
    CONSTRAINT chk_catalog_de_service_version CHECK (current_version >= 1)
);

CREATE INDEX IF NOT EXISTS idx_catalog_de_service_status ON catalog_de_service (status);
CREATE INDEX IF NOT EXISTS idx_catalog_de_service_category ON catalog_de_service (category);

INSERT INTO catalog_de_service (id, code, name, description, category, status, current_version, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'ACCOUNT_TRANSFER', 'Transfert de compte',
     'Transfert entre comptes (domestique)', 'TRANSFER', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'BILL_PAYMENT', 'Paiement de facture',
     'Paiement facturier (lookup + acquittement)', 'BILLING', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'INTERNATIONAL_TRANSFER', 'Transfert international',
     'Transfert cross-border / SWIFT', 'TRANSFER', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'REMITTANCE', 'Remittance',
     'Envoi d''argent / remittance', 'REMITTANCE', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'BANK_TO_WALLET', 'Banque vers wallet',
     'Alimentation wallet depuis un compte bancaire', 'TRANSFER', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'WALLET_TO_BANK', 'Wallet vers banque',
     'Retrait wallet vers compte bancaire', 'TRANSFER', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'CASH_OUT', 'Cash-out',
     'Retrait cash', 'CASH', 'PUBLISHED', 1, NOW(), NOW()),
    (gen_random_uuid(), 'CASH_IN', 'Cash-in',
     'Dépôt cash', 'CASH', 'PUBLISHED', 1, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
