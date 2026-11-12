-- Moteur de règles métier / réglementaires (base dédiée payment_rules).
-- V1 : règles versionnées, groupes, plafonds, cumuls, réservations, audit d'évaluation.

CREATE TABLE rule_definition (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    libelle         VARCHAR(200) NOT NULL,
    description     VARCHAR(500),
    category        VARCHAR(32)  NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'BROUILLON',
    legal_ref       VARCHAR(200),
    legal_text_no   VARCHAR(80),
    legal_effective DATE,
    legal_expiry    DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rule_version (
    id                  UUID PRIMARY KEY,
    rule_id             UUID         NOT NULL REFERENCES rule_definition (id),
    version             INT          NOT NULL,
    priority            INT          NOT NULL DEFAULT 100,
    conditions_json     TEXT         NOT NULL DEFAULT '{}',
    source_type         VARCHAR(40)  NOT NULL,
    source_period       VARCHAR(32),
    source_scope        VARCHAR(32),
    include_current     BOOLEAN      NOT NULL DEFAULT TRUE,
    operator            VARCHAR(16)  NOT NULL,
    limit_value         NUMERIC(18, 4),
    limit_values_json   TEXT,
    action_on_violation VARCHAR(16)  NOT NULL DEFAULT 'REJECT',
    message             VARCHAR(500) NOT NULL,
    mandatory           BOOLEAN      NOT NULL DEFAULT TRUE,
    effective_from      DATE         NOT NULL DEFAULT CURRENT_DATE,
    effective_to        DATE,
    status              VARCHAR(32)  NOT NULL DEFAULT 'BROUILLON',
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rule_version UNIQUE (rule_id, version)
);

CREATE INDEX idx_rule_version_status ON rule_version (status);

CREATE TABLE rule_group (
    id                  UUID PRIMARY KEY,
    code                VARCHAR(64)  NOT NULL UNIQUE,
    libelle             VARCHAR(200) NOT NULL,
    description         VARCHAR(500),
    transaction_type    VARCHAR(64),
    product_code        VARCHAR(80),
    evaluation_strategy VARCHAR(32)  NOT NULL DEFAULT 'STOP_ON_REJECT',
    status              VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rule_group_item (
    id              UUID PRIMARY KEY,
    group_id        UUID NOT NULL REFERENCES rule_group (id) ON DELETE CASCADE,
    rule_version_id UUID NOT NULL REFERENCES rule_version (id),
    sort_order      INT  NOT NULL DEFAULT 100,
    CONSTRAINT uq_rule_group_item UNIQUE (group_id, rule_version_id)
);

CREATE INDEX idx_rule_group_item_group ON rule_group_item (group_id);

CREATE TABLE limit_definition (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    libelle         VARCHAR(200) NOT NULL,
    period          VARCHAR(32)  NOT NULL,
    scope           VARCHAR(32)  NOT NULL,
    metric          VARCHAR(32)  NOT NULL DEFAULT 'AMOUNT',
    limit_value     NUMERIC(18, 4) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'XOF',
    transaction_type VARCHAR(64),
    kyc_level       VARCHAR(32),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE limit_counter (
    id              UUID PRIMARY KEY,
    limit_code      VARCHAR(64)  NOT NULL,
    scope_key       VARCHAR(200) NOT NULL,
    period_key      VARCHAR(64)  NOT NULL,
    amount_sum      NUMERIC(18, 4) NOT NULL DEFAULT 0,
    count_sum       INT          NOT NULL DEFAULT 0,
    reserved_amount NUMERIC(18, 4) NOT NULL DEFAULT 0,
    reserved_count  INT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_limit_counter UNIQUE (limit_code, scope_key, period_key)
);

CREATE INDEX idx_limit_counter_lookup ON limit_counter (limit_code, scope_key, period_key);

CREATE TABLE limit_reservation (
    id              UUID PRIMARY KEY,
    transaction_id  VARCHAR(80)  NOT NULL,
    limit_code      VARCHAR(64)  NOT NULL,
    scope_key       VARCHAR(200) NOT NULL,
    period_key      VARCHAR(64)  NOT NULL,
    amount          NUMERIC(18, 4) NOT NULL DEFAULT 0,
    count_units     INT          NOT NULL DEFAULT 1,
    status          VARCHAR(32)  NOT NULL DEFAULT 'HELD',
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    confirmed_at    TIMESTAMPTZ,
    released_at     TIMESTAMPTZ
);

CREATE INDEX idx_limit_reservation_txn ON limit_reservation (transaction_id);
CREATE INDEX idx_limit_reservation_status ON limit_reservation (status, expires_at);

CREATE TABLE rule_execution (
    id              UUID PRIMARY KEY,
    transaction_id  VARCHAR(80)  NOT NULL,
    group_code      VARCHAR(64),
    decision        VARCHAR(16)  NOT NULL,
    authorized      BOOLEAN      NOT NULL,
    simulate        BOOLEAN      NOT NULL DEFAULT FALSE,
    reservation_id  UUID,
    request_json    TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rule_execution_txn ON rule_execution (transaction_id);

CREATE TABLE rule_violation (
    id              UUID PRIMARY KEY,
    execution_id    UUID         NOT NULL REFERENCES rule_execution (id) ON DELETE CASCADE,
    rule_code       VARCHAR(64)  NOT NULL,
    rule_version    INT          NOT NULL,
    action          VARCHAR(16)  NOT NULL,
    message         VARCHAR(500) NOT NULL,
    current_value   NUMERIC(18, 4),
    requested_value NUMERIC(18, 4),
    projected_value NUMERIC(18, 4),
    limit_value     NUMERIC(18, 4)
);

CREATE TABLE rule_audit (
    id              UUID PRIMARY KEY,
    entity_type     VARCHAR(40)  NOT NULL,
    entity_id       UUID         NOT NULL,
    action          VARCHAR(40)  NOT NULL,
    actor           VARCHAR(120),
    detail_json     TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Seed V1 : groupe CASH_OUT_STANDARD + plafond journalier (valeur fictive) ─

INSERT INTO rule_definition (id, code, libelle, description, category, status, legal_ref)
VALUES
    ('a1000001-0000-4000-8000-000000000001', 'CHECK_CUSTOMER_STATUS',
     'Client actif', 'Le client doit être ACTIVE', 'RESTRICTION', 'PUBLIEE', NULL),
    ('a1000001-0000-4000-8000-000000000002', 'CHECK_KYC_LEVEL',
     'Niveau KYC autorisé', 'KYC STANDARD ou supérieur pour cash-out', 'KYC', 'PUBLIEE',
     'Instruction fictive — à valider Conformité'),
    ('a1000001-0000-4000-8000-000000000003', 'CHECK_TRANSACTION_LIMIT',
     'Montant max par opération', 'Plafond unitaire cash-out', 'LIMIT', 'PUBLIEE',
     'Instruction fictive — à valider Conformité'),
    ('a1000001-0000-4000-8000-000000000004', 'CASHOUT_DAILY_LIMIT',
     'Plafond journalier des retraits', 'Cumul journalier wallet (valeur fictive 500 000)', 'LIMIT', 'PUBLIEE',
     'Instruction fictive — à valider Conformité'),
    ('a1000001-0000-4000-8000-000000000005', 'CHECK_WALLET_BALANCE',
     'Solde disponible', 'Solde projeté >= 0 (solde fourni dans le contexte)', 'LIMIT', 'PUBLIEE', NULL);

INSERT INTO rule_version (id, rule_id, version, priority, conditions_json, source_type, source_period,
                          source_scope, include_current, operator, limit_value, limit_values_json,
                          action_on_violation, message, mandatory, status, published_at)
VALUES
    ('a2000001-0000-4000-8000-000000000001', 'a1000001-0000-4000-8000-000000000001', 1, 10,
     '{"transactionType":"CASH_OUT"}', 'CUSTOMER_STATUS', NULL, NULL, false,
     'EQ', NULL, '["ACTIVE"]', 'REJECT', 'Client non actif', true, 'PUBLIEE', NOW()),
    ('a2000001-0000-4000-8000-000000000002', 'a1000001-0000-4000-8000-000000000002', 1, 20,
     '{"transactionType":"CASH_OUT"}', 'KYC_LEVEL', NULL, NULL, false,
     'IN', NULL, '["STANDARD","ENHANCED","FULL"]', 'REJECT', 'Niveau KYC insuffisant', true, 'PUBLIEE', NOW()),
    ('a2000001-0000-4000-8000-000000000003', 'a1000001-0000-4000-8000-000000000003', 1, 30,
     '{"transactionType":"CASH_OUT","kycLevel":"STANDARD"}', 'TRANSACTION_AMOUNT', NULL, NULL, true,
     'LTE', 200000, NULL, 'REJECT', 'Montant supérieur au plafond unitaire', true, 'PUBLIEE', NOW()),
    ('a2000001-0000-4000-8000-000000000004', 'a1000001-0000-4000-8000-000000000004', 1, 40,
     '{"transactionType":"CASH_OUT","kycLevel":"STANDARD"}', 'CUMUL_AMOUNT', 'DAY', 'WALLET', true,
     'LTE', 500000, NULL, 'REJECT', 'Plafond journalier dépassé', true, 'PUBLIEE', NOW()),
    ('a2000001-0000-4000-8000-000000000005', 'a1000001-0000-4000-8000-000000000005', 1, 60,
     '{"transactionType":"CASH_OUT"}', 'PROJECTED_BALANCE', NULL, 'WALLET', true,
     'GTE', 0, NULL, 'REJECT', 'Solde insuffisant', true, 'PUBLIEE', NOW());

INSERT INTO rule_group (id, code, libelle, description, transaction_type, evaluation_strategy, status)
VALUES ('a3000001-0000-4000-8000-000000000001', 'CASH_OUT_STANDARD',
        'Contrôles cash-out standard', 'Groupe CDC V1 — valeurs fictives',
        'CASH_OUT', 'STOP_ON_REJECT', 'ACTIVE');

INSERT INTO rule_group_item (id, group_id, rule_version_id, sort_order) VALUES
    ('a4000001-0000-4000-8000-000000000001', 'a3000001-0000-4000-8000-000000000001',
     'a2000001-0000-4000-8000-000000000001', 10),
    ('a4000001-0000-4000-8000-000000000002', 'a3000001-0000-4000-8000-000000000001',
     'a2000001-0000-4000-8000-000000000002', 20),
    ('a4000001-0000-4000-8000-000000000003', 'a3000001-0000-4000-8000-000000000001',
     'a2000001-0000-4000-8000-000000000003', 30),
    ('a4000001-0000-4000-8000-000000000004', 'a3000001-0000-4000-8000-000000000001',
     'a2000001-0000-4000-8000-000000000004', 40),
    ('a4000001-0000-4000-8000-000000000005', 'a3000001-0000-4000-8000-000000000001',
     'a2000001-0000-4000-8000-000000000005', 60);

INSERT INTO limit_definition (id, code, libelle, period, scope, metric, limit_value, currency, transaction_type, kyc_level)
VALUES ('a5000001-0000-4000-8000-000000000001', 'CASHOUT_DAILY_LIMIT',
        'Plafond journalier retraits (fictif)', 'DAY', 'WALLET', 'AMOUNT', 500000, 'XOF', 'CASH_OUT', 'STANDARD');
