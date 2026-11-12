-- Profils d'offre : services disponibles et grilles tarifaires de référence.
-- Un profil est un gabarit ; il s'affecte à une Party (client, agent, org…).
-- Les codes service / grille sont des références stables vers le catalogue gateway et payment-pricing ;
-- identity ne les valide pas (pas d'appel croisé).

CREATE TABLE identity_profile (
    id            UUID PRIMARY KEY,
    tenant_id     VARCHAR(64)  NOT NULL,
    code          VARCHAR(64)  NOT NULL,
    libelle       VARCHAR(200) NOT NULL,
    description   VARCHAR(500),
    audience      VARCHAR(32),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_profile_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_identity_profile_tenant ON identity_profile (tenant_id);

-- Produits / services du catalogue que le profil autorise (ex. ACCOUNT_TRANSFER, CASH_IN).
CREATE TABLE identity_profile_service (
    id            UUID PRIMARY KEY,
    profile_id    UUID         NOT NULL REFERENCES identity_profile (id) ON DELETE CASCADE,
    service_code  VARCHAR(80)  NOT NULL,
    libelle       VARCHAR(200),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_identity_profile_service UNIQUE (profile_id, service_code)
);

CREATE INDEX idx_identity_profile_service_profile ON identity_profile_service (profile_id);

-- Grille tarifaire de référence pour un produit (code de grille payment-pricing, stable entre versions).
CREATE TABLE identity_profile_grid (
    id            UUID PRIMARY KEY,
    profile_id    UUID         NOT NULL REFERENCES identity_profile (id) ON DELETE CASCADE,
    produit_code  VARCHAR(80)  NOT NULL,
    grille_code   VARCHAR(64)  NOT NULL,
    canal_code    VARCHAR(40)  NOT NULL DEFAULT '',
    devise        VARCHAR(3),
    CONSTRAINT uq_identity_profile_grid UNIQUE (profile_id, produit_code, canal_code)
);

CREATE INDEX idx_identity_profile_grid_profile ON identity_profile_grid (profile_id);

-- Affectation Party ↔ Profil (une Party peut cumuler plusieurs profils ; l'offre = union).
CREATE TABLE identity_party_profile (
    id            UUID PRIMARY KEY,
    party_id      UUID         NOT NULL REFERENCES identity_party (id),
    profile_id    UUID         NOT NULL REFERENCES identity_profile (id),
    date_debut    DATE         NOT NULL DEFAULT CURRENT_DATE,
    date_fin      DATE,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_party_profile UNIQUE (party_id, profile_id)
);

CREATE INDEX idx_identity_party_profile_party ON identity_party_profile (party_id);
CREATE INDEX idx_identity_party_profile_profile ON identity_party_profile (profile_id);

-- ── Seed démo ───────────────────────────────────────────────────────────────

INSERT INTO identity_profile (id, tenant_id, code, libelle, description, audience, active)
VALUES
    ('f1000001-0000-4000-8000-000000000001', 'CORIS_DEMO', 'RETAIL-STD',
     'Retail standard', 'Wallet et paiements du particulier', 'RETAIL', TRUE),
    ('f1000001-0000-4000-8000-000000000002', 'CORIS_DEMO', 'AGENT-STD',
     'Agent standard', 'Cash-in / cash-out et transferts agent', 'AGENT', TRUE),
    ('f1000001-0000-4000-8000-000000000003', 'CORIS_DEMO', 'CORPORATE-STD',
     'Corporate standard', 'Virements et paiements de masse', 'CORPORATE', TRUE);

INSERT INTO identity_profile_service (id, profile_id, service_code, libelle, active)
VALUES
    ('f2000001-0000-4000-8000-000000000001', 'f1000001-0000-4000-8000-000000000001', 'BILL_PAYMENT', 'Paiement de facture', TRUE),
    ('f2000001-0000-4000-8000-000000000002', 'f1000001-0000-4000-8000-000000000001', 'ACCOUNT_TRANSFER', 'Transfert compte', TRUE),
    ('f2000001-0000-4000-8000-000000000003', 'f1000001-0000-4000-8000-000000000002', 'CASH_IN', 'Dépôt agent', TRUE),
    ('f2000001-0000-4000-8000-000000000004', 'f1000001-0000-4000-8000-000000000002', 'CASH_OUT', 'Retrait agent', TRUE),
    ('f2000001-0000-4000-8000-000000000005', 'f1000001-0000-4000-8000-000000000002', 'TRANSFERT_INTL', 'Transfert international', TRUE),
    ('f2000001-0000-4000-8000-000000000006', 'f1000001-0000-4000-8000-000000000003', 'ACCOUNT_TRANSFER', 'Transfert compte', TRUE),
    ('f2000001-0000-4000-8000-000000000007', 'f1000001-0000-4000-8000-000000000003', 'BILL_PAYMENT', 'Paiement de facture', TRUE);

INSERT INTO identity_profile_grid (id, profile_id, produit_code, grille_code, canal_code, devise)
VALUES
    ('f3000001-0000-4000-8000-000000000001', 'f1000001-0000-4000-8000-000000000001', 'BILL_PAYMENT', 'FEE-BILL', '', 'XOF'),
    ('f3000001-0000-4000-8000-000000000002', 'f1000001-0000-4000-8000-000000000001', 'ACCOUNT_TRANSFER', 'FEE-DOMESTIC', 'WEB', 'XOF'),
    ('f3000001-0000-4000-8000-000000000003', 'f1000001-0000-4000-8000-000000000002', 'CASH_IN', 'FEE-CASH-AGENT', '', 'XOF'),
    ('f3000001-0000-4000-8000-000000000004', 'f1000001-0000-4000-8000-000000000002', 'TRANSFERT_INTL', 'DEMO-INTL-MASTER', '', 'XOF'),
    ('f3000001-0000-4000-8000-000000000005', 'f1000001-0000-4000-8000-000000000003', 'ACCOUNT_TRANSFER', 'FEE-DOMESTIC', 'WEB', 'XOF'),
    ('f3000001-0000-4000-8000-000000000006', 'f1000001-0000-4000-8000-000000000003', 'BILL_PAYMENT', 'FEE-BILL', '', 'XOF');

-- Affectations démo : Awa → retail, Agent → agent, Jean (corporate) → corporate
INSERT INTO identity_party_profile (id, party_id, profile_id, date_debut, active)
VALUES
    ('f4000001-0000-4000-8000-000000000001', 'a1000001-0000-4000-8000-000000000006',
     'f1000001-0000-4000-8000-000000000001', DATE '2026-01-01', TRUE),
    ('f4000001-0000-4000-8000-000000000002', 'a1000001-0000-4000-8000-000000000005',
     'f1000001-0000-4000-8000-000000000002', DATE '2026-01-01', TRUE),
    ('f4000001-0000-4000-8000-000000000003', 'a1000001-0000-4000-8000-000000000002',
     'f1000001-0000-4000-8000-000000000003', DATE '2026-01-01', TRUE);
