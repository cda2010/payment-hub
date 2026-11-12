-- payment-identity (base dédiée) — parties, org tree, account aliases, channel users, memberships
-- Reprise à l'identique de V111__identity_module.sql de payment-channel-api (mêmes UUID de seed).
-- Tenant démo : CORIS_DEMO (aligné arbre corporate ABC Holding)

CREATE TABLE IF NOT EXISTS identity_party (
    id            UUID PRIMARY KEY,
    tenant_id     VARCHAR(64)  NOT NULL,
    code          VARCHAR(64)  NOT NULL,
    kind          VARCHAR(32)  NOT NULL,
    display_name  VARCHAR(200) NOT NULL,
    phone         VARCHAR(30),
    email         VARCHAR(180),
    external_ref  VARCHAR(40),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_party_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_identity_party_phone ON identity_party (phone);

CREATE TABLE IF NOT EXISTS identity_org_node (
    id          UUID PRIMARY KEY,
    tenant_id   VARCHAR(64)  NOT NULL,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    type        VARCHAR(32)  NOT NULL,
    parent_id   UUID REFERENCES identity_org_node (id),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_org_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_identity_org_parent ON identity_org_node (parent_id);

CREATE TABLE IF NOT EXISTS identity_account_alias (
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
    CONSTRAINT uq_identity_alias_tenant_code UNIQUE (tenant_id, alias_code)
);

CREATE INDEX IF NOT EXISTS idx_identity_alias_owner ON identity_account_alias (owner_kind, owner_id);

CREATE TABLE IF NOT EXISTS identity_channel_user (
    id             UUID PRIMARY KEY,
    tenant_id      VARCHAR(64)  NOT NULL,
    username       VARCHAR(100) NOT NULL,
    party_id       UUID         NOT NULL REFERENCES identity_party (id),
    audience       VARCHAR(32)  NOT NULL,
    role           VARCHAR(64)  NOT NULL,
    password_hash  VARCHAR(255),
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_login_at  TIMESTAMPTZ,
    CONSTRAINT uq_identity_user_tenant_username UNIQUE (tenant_id, username)
);

CREATE INDEX IF NOT EXISTS idx_identity_user_party ON identity_channel_user (party_id);

CREATE TABLE IF NOT EXISTS identity_membership (
    id           UUID PRIMARY KEY,
    user_id      UUID        NOT NULL REFERENCES identity_channel_user (id),
    org_node_id  UUID        NOT NULL REFERENCES identity_org_node (id),
    scope        VARCHAR(32) NOT NULL DEFAULT 'DIRECT',
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_membership UNIQUE (user_id, org_node_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_membership_user ON identity_membership (user_id);
CREATE INDEX IF NOT EXISTS idx_identity_membership_org ON identity_membership (org_node_id);

-- ── Seed démo (idempotent) ─────────────────────────────────────────────────
-- UUIDs fixes pour mapping front corporate

-- Parties
INSERT INTO identity_party (id, tenant_id, code, kind, display_name, phone, email, external_ref, active)
SELECT v.id::uuid, 'CORIS_DEMO', v.code, v.kind, v.display_name, v.phone, v.email, v.external_ref, TRUE
FROM (VALUES
    ('a1000001-0000-4000-8000-000000000001', 'ORG-ABC', 'ORGANIZATION', 'ABC Holding', NULL, 'contact@abc.demo', 'TENANT-ABC'),
    ('a1000001-0000-4000-8000-000000000002', 'PERS-JEAN', 'PERSON', 'Jean Kouassi', '70000001', 'jean@abc.demo', NULL),
    ('a1000001-0000-4000-8000-000000000003', 'PERS-PAUL', 'PERSON', 'Paul Ouédraogo', '70000002', 'paul@abc.demo', NULL),
    ('a1000001-0000-4000-8000-000000000004', 'PERS-MARIE', 'PERSON', 'Marie Traoré', '70000003', 'marie@abc.demo', NULL),
    ('a1000001-0000-4000-8000-000000000005', 'PERS-AGENT1', 'PERSON', 'Agent Kiosk Zone 4', '76000001', 'agent1@coris.demo', 'AG-4412'),
    ('a1000001-0000-4000-8000-000000000006', 'PERS-RETAIL1', 'PERSON', 'Awa Ouédraogo', '70123456', NULL, NULL)
) AS v(id, code, kind, display_name, phone, email, external_ref)
WHERE NOT EXISTS (SELECT 1 FROM identity_party WHERE tenant_id = 'CORIS_DEMO' LIMIT 1);

-- Org tree : Holding → BF / ML / CI
INSERT INTO identity_org_node (id, tenant_id, code, name, type, parent_id, active)
SELECT v.id::uuid, 'CORIS_DEMO', v.code, v.name, v.type, v.parent_id::uuid, TRUE
FROM (VALUES
    ('b1000001-0000-4000-8000-000000000001', 'H001', 'ABC Holding', 'GROUP', NULL),
    ('b1000001-0000-4000-8000-000000000002', 'BF001', 'ABC Burkina', 'LEGAL_ENTITY', 'b1000001-0000-4000-8000-000000000001'),
    ('b1000001-0000-4000-8000-000000000003', 'ML001', 'ABC Mali', 'LEGAL_ENTITY', 'b1000001-0000-4000-8000-000000000001'),
    ('b1000001-0000-4000-8000-000000000004', 'CI001', 'ABC Côte d’Ivoire', 'LEGAL_ENTITY', 'b1000001-0000-4000-8000-000000000001'),
    ('b1000001-0000-4000-8000-000000000005', 'AG-4412', 'Kiosque Zone 4', 'POS', NULL)
) AS v(id, code, name, type, parent_id)
WHERE NOT EXISTS (SELECT 1 FROM identity_org_node WHERE tenant_id = 'CORIS_DEMO' LIMIT 1);

-- Account aliases (org-owned)
INSERT INTO identity_account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency, owner_kind, owner_id, provider_code, active
)
SELECT v.id::uuid, 'CORIS_DEMO', v.alias_code, v.label, v.alias_type, v.external_ref, v.currency,
       'ORG', v.owner_id::uuid, v.provider_code, TRUE
FROM (VALUES
    ('c1000001-0000-4000-8000-000000000001', 'ALIAS-H001-C01', 'Compte consolidé', 'IBAN',
     'BF12 CORI 0000 0001 0001 0001', 'XOF', 'b1000001-0000-4000-8000-000000000001', 'CORIS_BANK'),
    ('c1000001-0000-4000-8000-000000000002', 'ALIAS-BF001-C01', 'Compte courant BF', 'IBAN',
     'BF12 CORI 0001 0001 0001 0001', 'XOF', 'b1000001-0000-4000-8000-000000000002', 'CORIS_BANK'),
    ('c1000001-0000-4000-8000-000000000003', 'ALIAS-BF001-C02', 'Compte salaires BF', 'IBAN',
     'BF12 CORI 0001 0001 0001 0002', 'XOF', 'b1000001-0000-4000-8000-000000000002', 'CORIS_BANK'),
    ('c1000001-0000-4000-8000-000000000004', 'ALIAS-ML001-C01', 'Compte courant ML', 'IBAN',
     'ML12 CORI 0002 0001 0001 0001', 'XOF', 'b1000001-0000-4000-8000-000000000003', 'CORIS_BANK'),
    ('c1000001-0000-4000-8000-000000000005', 'ALIAS-CI001-C01', 'Compte courant CI', 'IBAN',
     'CI93 CORI 0003 0001 0001 0001', 'XOF', 'b1000001-0000-4000-8000-000000000004', 'CORIS_BANK'),
    ('c1000001-0000-4000-8000-000000000006', 'ALIAS-TILL-4412', 'Caisse AG-4412', 'TILL',
     'TILL-AG-4412', 'XOF', 'b1000001-0000-4000-8000-000000000005', 'CORIS_MONEY')
) AS v(id, alias_code, label, alias_type, external_ref, currency, owner_id, provider_code)
WHERE NOT EXISTS (SELECT 1 FROM identity_account_alias WHERE tenant_id = 'CORIS_DEMO' LIMIT 1);

-- Alias wallet retail (owner = PARTY)
INSERT INTO identity_account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency, owner_kind, owner_id, provider_code, active
)
SELECT 'c1000001-0000-4000-8000-000000000007'::uuid, 'CORIS_DEMO', 'ALIAS-RETAIL-AWA', 'Wallet Awa',
       'WALLET', '00111000002612', 'XOF', 'PARTY',
       'a1000001-0000-4000-8000-000000000006'::uuid, 'CORIS_MONEY', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM identity_account_alias WHERE tenant_id = 'CORIS_DEMO' AND alias_code = 'ALIAS-RETAIL-AWA'
);

-- Channel users
INSERT INTO identity_channel_user (id, tenant_id, username, party_id, audience, role, enabled)
SELECT v.id::uuid, 'CORIS_DEMO', v.username, v.party_id::uuid, v.audience, v.role, TRUE
FROM (VALUES
    ('d1000001-0000-4000-8000-000000000001', 'jean', 'a1000001-0000-4000-8000-000000000002', 'CORPORATE', 'GROUP_ADMIN'),
    ('d1000001-0000-4000-8000-000000000002', 'paul', 'a1000001-0000-4000-8000-000000000003', 'CORPORATE', 'TREASURER'),
    ('d1000001-0000-4000-8000-000000000003', 'marie', 'a1000001-0000-4000-8000-000000000004', 'CORPORATE', 'ACCOUNTANT'),
    ('d1000001-0000-4000-8000-000000000004', 'agent1', 'a1000001-0000-4000-8000-000000000005', 'AGENT', 'CASHIER'),
    ('d1000001-0000-4000-8000-000000000005', 'awa', 'a1000001-0000-4000-8000-000000000006', 'RETAIL', 'CUSTOMER')
) AS v(id, username, party_id, audience, role)
WHERE NOT EXISTS (SELECT 1 FROM identity_channel_user WHERE tenant_id = 'CORIS_DEMO' LIMIT 1);

-- Memberships
INSERT INTO identity_membership (id, user_id, org_node_id, scope, active)
SELECT v.id::uuid, v.user_id::uuid, v.org_node_id::uuid, v.scope, TRUE
FROM (VALUES
    ('e1000001-0000-4000-8000-000000000001', 'd1000001-0000-4000-8000-000000000001', 'b1000001-0000-4000-8000-000000000001', 'SUBTREE'),
    ('e1000001-0000-4000-8000-000000000002', 'd1000001-0000-4000-8000-000000000002', 'b1000001-0000-4000-8000-000000000002', 'DESCENDANTS'),
    ('e1000001-0000-4000-8000-000000000003', 'd1000001-0000-4000-8000-000000000003', 'b1000001-0000-4000-8000-000000000002', 'DIRECT'),
    ('e1000001-0000-4000-8000-000000000004', 'd1000001-0000-4000-8000-000000000004', 'b1000001-0000-4000-8000-000000000005', 'DIRECT')
) AS v(id, user_id, org_node_id, scope)
WHERE NOT EXISTS (SELECT 1 FROM identity_membership LIMIT 1);
