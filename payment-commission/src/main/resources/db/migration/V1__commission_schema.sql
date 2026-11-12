-- Moteur de commissionnement (base dédiée payment_commission).
-- Lot 1 : formules, grilles, règles, seed cas de référence 1000 FCFA.

CREATE TABLE commission_formule (
    id            UUID PRIMARY KEY,
    code          VARCHAR(64)  NOT NULL,
    version       INT          NOT NULL,
    libelle       VARCHAR(200) NOT NULL,
    description   VARCHAR(500),
    type          VARCHAR(32)  NOT NULL,
    status        VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_commission_formule_code_version UNIQUE (code, version)
);

CREATE TABLE commission_formule_param (
    id              UUID PRIMARY KEY,
    formule_id      UUID         NOT NULL REFERENCES commission_formule (id) ON DELETE CASCADE,
    code            VARCHAR(64)  NOT NULL,
    libelle         VARCHAR(200) NOT NULL,
    data_type       VARCHAR(32)  NOT NULL,
    required        BOOLEAN      NOT NULL DEFAULT TRUE,
    default_value   TEXT,
    CONSTRAINT uq_commission_formule_param UNIQUE (formule_id, code)
);

CREATE TABLE commission_grille (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    version         INT          NOT NULL DEFAULT 1,
    libelle         VARCHAR(200) NOT NULL,
    description     VARCHAR(500),
    operation_type  VARCHAR(64)  NOT NULL,
    product_code    VARCHAR(80),
    channel_code    VARCHAR(40),
    currency        VARCHAR(3)   NOT NULL DEFAULT 'XOF',
    status          VARCHAR(32)  NOT NULL DEFAULT 'BROUILLON',
    valid_from      DATE         NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE,
    envelope_mode   VARCHAR(32)  NOT NULL DEFAULT 'FULL_FEES',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_commission_grille_code_version UNIQUE (code, version)
);

CREATE INDEX idx_commission_grille_status ON commission_grille (status);
CREATE INDEX idx_commission_grille_op ON commission_grille (operation_type, status);

CREATE TABLE commission_regle (
    id                      UUID PRIMARY KEY,
    grille_id               UUID         NOT NULL REFERENCES commission_grille (id) ON DELETE CASCADE,
    code                    VARCHAR(64)  NOT NULL,
    libelle                 VARCHAR(200) NOT NULL,
    beneficiary_role        VARCHAR(64)  NOT NULL,
    beneficiary_owner_kind  VARCHAR(16),
    beneficiary_owner_id    UUID,
    formule_id              UUID         NOT NULL REFERENCES commission_formule (id),
    commission_base         VARCHAR(32)  NOT NULL DEFAULT 'TRANSACTION_FEES',
    tier_selection_source   VARCHAR(32),
    funding_type            VARCHAR(32)  NOT NULL DEFAULT 'TRANSACTION_FEES',
    funder_role             VARCHAR(64),
    params_json             TEXT         NOT NULL DEFAULT '{}',
    floor_amount            NUMERIC(18, 4),
    cap_amount              NUMERIC(18, 4),
    priority                INT          NOT NULL DEFAULT 100,
    active                  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_commission_regle_grille_code UNIQUE (grille_id, code)
);

CREATE INDEX idx_commission_regle_grille ON commission_regle (grille_id);

-- ── Formules prédéfinies ────────────────────────────────────────────────────

INSERT INTO commission_formule (id, code, version, libelle, description, type, status)
VALUES
    ('c1000001-0000-4000-8000-000000000001', 'FIXED', 1,
     'Montant fixe', 'C = montantFixe', 'FIXED', 'ACTIVE'),
    ('c1000001-0000-4000-8000-000000000002', 'PERCENT', 1,
     'Pourcentage de la base', 'C = base × taux', 'PERCENT', 'ACTIVE'),
    ('c1000001-0000-4000-8000-000000000003', 'HYBRID', 1,
     'Pourcentage + fixe', 'C = base × taux + montantFixe', 'HYBRID', 'ACTIVE');

INSERT INTO commission_formule_param (id, formule_id, code, libelle, data_type, required, default_value)
VALUES
    ('c2000001-0000-4000-8000-000000000001', 'c1000001-0000-4000-8000-000000000001',
     'montantFixe', 'Montant fixe', 'DECIMAL', TRUE, NULL),
    ('c2000001-0000-4000-8000-000000000002', 'c1000001-0000-4000-8000-000000000002',
     'taux', 'Taux décimal (0.20 = 20 %)', 'DECIMAL', TRUE, NULL),
    ('c2000001-0000-4000-8000-000000000003', 'c1000001-0000-4000-8000-000000000003',
     'taux', 'Taux décimal', 'DECIMAL', TRUE, NULL),
    ('c2000001-0000-4000-8000-000000000004', 'c1000001-0000-4000-8000-000000000003',
     'montantFixe', 'Montant fixe', 'DECIMAL', TRUE, '0');

-- ── Grille démo : répartition 1000 FCFA de frais (cas R-01) ─────────────────

INSERT INTO commission_grille (id, code, version, libelle, description, operation_type, product_code,
                               channel_code, currency, status, envelope_mode)
VALUES
    ('c3000001-0000-4000-8000-000000000001', 'CASH_OUT-STD', 1,
     'Cash-out standard — répartition frais',
     'Cas de référence CDC : 40/25/20/10/5 % sur les frais transaction',
     'CASH_OUT', 'CASH_OUT', 'AGENT', 'XOF', 'PUBLIEE', 'FULL_FEES');

INSERT INTO commission_regle (id, grille_id, code, libelle, beneficiary_role, formule_id,
                              commission_base, funding_type, params_json, priority)
VALUES
    ('c4000001-0000-4000-8000-000000000001', 'c3000001-0000-4000-8000-000000000001',
     'EME', 'Part émetteur', 'ISSUER',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.40}', 10),
    ('c4000001-0000-4000-8000-000000000002', 'c3000001-0000-4000-8000-000000000001',
     'DIST', 'Part distributeur principal', 'DISTRIBUTOR',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.25}', 20),
    ('c4000001-0000-4000-8000-000000000003', 'c3000001-0000-4000-8000-000000000001',
     'AGENT', 'Part agent', 'AGENT',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.20}', 30),
    ('c4000001-0000-4000-8000-000000000004', 'c3000001-0000-4000-8000-000000000001',
     'AGG', 'Part agrégateur', 'AGGREGATOR',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.10}', 40),
    ('c4000001-0000-4000-8000-000000000005', 'c3000001-0000-4000-8000-000000000001',
     'PROC', 'Part processeur', 'PROCESSOR',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.05}', 50);
