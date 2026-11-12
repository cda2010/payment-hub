-- Alias de comptes : pointeurs stables vers CBS / Coris / till.
-- Propriétaire : Party ou Org (payment-identity) ou unité d'organisation (payment-network).
-- Ce module ne valide pas l'existence du propriétaire (référence UUID + kind).

CREATE TABLE account_alias (
    id             UUID PRIMARY KEY,
    tenant_id      VARCHAR(64)  NOT NULL,
    alias_code     VARCHAR(80)  NOT NULL,
    label          VARCHAR(200) NOT NULL,
    alias_type     VARCHAR(32)  NOT NULL,
    external_ref   VARCHAR(120) NOT NULL,
    currency       VARCHAR(8)   NOT NULL DEFAULT 'XOF',
    owner_kind     VARCHAR(16)  NOT NULL,
    owner_id       UUID         NOT NULL,
    provider_code  VARCHAR(50),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_account_alias_tenant_code UNIQUE (tenant_id, alias_code),
    CONSTRAINT ck_account_alias_owner_kind CHECK (owner_kind IN ('PARTY', 'ORG', 'UNITE'))
);

CREATE INDEX idx_account_alias_owner ON account_alias (owner_kind, owner_id);
CREATE INDEX idx_account_alias_tenant ON account_alias (tenant_id);

-- Seed : reprise des alias identity (ORG / PARTY) + un alias réseau (UNITE = MS-SAHEL).
INSERT INTO account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency, owner_kind, owner_id, provider_code, active
)
VALUES
    ('c1000001-0000-4000-8000-000000000001', 'CORIS_DEMO', 'ALIAS-H001-C01', 'Compte consolidé', 'IBAN',
     'BF12 CORI 0000 0001 0001 0001', 'XOF', 'ORG', 'b1000001-0000-4000-8000-000000000001', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000002', 'CORIS_DEMO', 'ALIAS-BF001-C01', 'Compte courant BF', 'IBAN',
     'BF12 CORI 0001 0001 0001 0001', 'XOF', 'ORG', 'b1000001-0000-4000-8000-000000000002', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000003', 'CORIS_DEMO', 'ALIAS-BF001-C02', 'Compte salaires BF', 'IBAN',
     'BF12 CORI 0001 0001 0001 0002', 'XOF', 'ORG', 'b1000001-0000-4000-8000-000000000002', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000004', 'CORIS_DEMO', 'ALIAS-ML001-C01', 'Compte courant ML', 'IBAN',
     'ML12 CORI 0002 0001 0001 0001', 'XOF', 'ORG', 'b1000001-0000-4000-8000-000000000003', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000005', 'CORIS_DEMO', 'ALIAS-CI001-C01', 'Compte courant CI', 'IBAN',
     'CI93 CORI 0003 0001 0001 0001', 'XOF', 'ORG', 'b1000001-0000-4000-8000-000000000004', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000006', 'CORIS_DEMO', 'ALIAS-TILL-4412', 'Caisse AG-4412 (org identity)', 'TILL',
     'TILL-AG-4412', 'XOF', 'ORG', 'b1000001-0000-4000-8000-000000000005', 'CORIS_MONEY', TRUE),
    ('c1000001-0000-4000-8000-000000000007', 'CORIS_DEMO', 'ALIAS-RETAIL-AWA', 'Wallet Awa', 'WALLET',
     '00111000002612', 'XOF', 'PARTY', 'a1000001-0000-4000-8000-000000000006', 'CORIS_MONEY', TRUE),
    ('c1000001-0000-4000-8000-000000000008', 'CORIS_DEMO', 'ALIAS-RETAIL-AWA-2', 'Wallet Awa 2', 'WALLET',
     '0011000000001', 'XOF', 'PARTY', 'a1000001-0000-4000-8000-000000000006', 'CORIS_MONEY', TRUE),
    -- Unité réseau MS-SAHEL (payment-network)
    ('c1000001-0000-4000-8000-000000000010', 'CORIS_DEMO', 'ALIAS-MS-SAHEL-TILL', 'Till Master Sahel', 'TILL',
     'TILL-MS-SAHEL', 'XOF', 'UNITE', '10000000-0000-0000-0000-000000000021', 'CORIS_MONEY', TRUE);
