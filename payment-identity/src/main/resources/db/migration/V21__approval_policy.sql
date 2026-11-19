-- Politiques d'approbation rattachées à un nœud de l'arbre.
-- La filiale surcharge le holding : la résolution remonte enfant → parent.

CREATE TABLE identity_approval_policy (
    id             UUID PRIMARY KEY,
    tenant_id      VARCHAR(64)  NOT NULL,
    org_node_id    UUID         NOT NULL REFERENCES identity_org_node (id),
    product_code   VARCHAR(64)  NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    override_flag  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_approval_policy UNIQUE (org_node_id, product_code)
);

CREATE INDEX idx_identity_approval_policy_tenant ON identity_approval_policy (tenant_id);

CREATE TABLE identity_approval_rule (
    id                  UUID PRIMARY KEY,
    policy_id           UUID         NOT NULL REFERENCES identity_approval_policy (id) ON DELETE CASCADE,
    code                VARCHAR(64)  NOT NULL,
    label               VARCHAR(160) NOT NULL,
    position            INT          NOT NULL,
    total_amount_lte    BIGINT,
    line_count_lte      INT,
    max_line_amount_lte BIGINT,
    else_rule           BOOLEAN      NOT NULL DEFAULT FALSE,
    profile             VARCHAR(32)  NOT NULL,
    CONSTRAINT uq_identity_approval_rule UNIQUE (policy_id, code)
);

CREATE INDEX idx_identity_approval_rule_policy ON identity_approval_rule (policy_id, position);

CREATE TABLE identity_approval_assignee (
    id              UUID PRIMARY KEY,
    policy_id       UUID        NOT NULL REFERENCES identity_approval_policy (id) ON DELETE CASCADE,
    level           VARCHAR(8)  NOT NULL,
    assignee_type   VARCHAR(16) NOT NULL,
    assignee_value  VARCHAR(64) NOT NULL,
    assignee_scope  VARCHAR(16) NOT NULL,
    CONSTRAINT uq_identity_approval_assignee UNIQUE (policy_id, level)
);

-- Annuaire statique : Jean, Paul, Marie (les comptes canal existent déjà).
UPDATE identity_party
SET display_name = 'Jean', email = 'jean@abc-holding.bf', updated_at = NOW()
WHERE id = 'a1000001-0000-4000-8000-000000000002';

UPDATE identity_party
SET display_name = 'Paul', email = 'paul@abc-holding.bf', updated_at = NOW()
WHERE id = 'a1000001-0000-4000-8000-000000000003';

UPDATE identity_party
SET display_name = 'Marie', email = 'marie@abc-burkina.bf', updated_at = NOW()
WHERE id = 'a1000001-0000-4000-8000-000000000004';

UPDATE identity_membership
SET scope = 'DIRECT'
WHERE id = 'e1000001-0000-4000-8000-000000000002';

INSERT INTO identity_membership (id, user_id, org_node_id, scope, active)
VALUES (
    'e1000001-0000-4000-8000-000000000016',
    'd1000001-0000-4000-8000-000000000002',
    'b1000001-0000-4000-8000-000000000003',
    'DIRECT',
    TRUE
)
ON CONFLICT (id) DO NOTHING;

-- Holding : petit lot direct, lot standard un checker, gros lot deux niveaux.
INSERT INTO identity_approval_policy (id, tenant_id, org_node_id, product_code, enabled, override_flag)
VALUES (
    'f1000001-0000-4000-8000-000000000001',
    'CORIS_DEMO',
    'b1000001-0000-4000-8000-000000000001',
    'PAIEMENT_EN_MASSE',
    TRUE,
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO identity_approval_rule (id, policy_id, code, label, position, total_amount_lte, line_count_lte, else_rule, profile)
VALUES
    ('f1000001-0000-4000-8000-000000000011', 'f1000001-0000-4000-8000-000000000001',
     'r-small', 'Petit lot auto', 0, 500000, 5, FALSE, 'NO_APPROVAL'),
    ('f1000001-0000-4000-8000-000000000012', 'f1000001-0000-4000-8000-000000000001',
     'r-mid', 'Lot standard — 1 checker', 1, 5000000, NULL, FALSE, 'L1_CHECKER'),
    ('f1000001-0000-4000-8000-000000000013', 'f1000001-0000-4000-8000-000000000001',
     'r-else', 'Gros lot — 2 niveaux', 2, NULL, NULL, TRUE, 'L1_L2')
ON CONFLICT (id) DO NOTHING;

-- Burkina surcharge le holding.
INSERT INTO identity_approval_policy (id, tenant_id, org_node_id, product_code, enabled, override_flag)
VALUES (
    'f1000001-0000-4000-8000-000000000002',
    'CORIS_DEMO',
    'b1000001-0000-4000-8000-000000000002',
    'PAIEMENT_EN_MASSE',
    TRUE,
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO identity_approval_rule (id, policy_id, code, label, position, total_amount_lte, line_count_lte, else_rule, profile)
VALUES
    ('f1000001-0000-4000-8000-000000000021', 'f1000001-0000-4000-8000-000000000002',
     'bf-mid', 'BF — toujours checker', 0, 2000000, NULL, FALSE, 'L1_CHECKER'),
    ('f1000001-0000-4000-8000-000000000022', 'f1000001-0000-4000-8000-000000000002',
     'bf-else', 'BF — gros lot double validation', 1, NULL, NULL, TRUE, 'L1_L2')
ON CONFLICT (id) DO NOTHING;

INSERT INTO identity_approval_assignee (id, policy_id, level, assignee_type, assignee_value, assignee_scope)
VALUES
    ('f1000001-0000-4000-8000-000000000031', 'f1000001-0000-4000-8000-000000000001', 'L1', 'ROLE', 'CHECKER', 'ENTITY'),
    ('f1000001-0000-4000-8000-000000000032', 'f1000001-0000-4000-8000-000000000001', 'L2', 'ROLE', 'ADMIN', 'TENANT'),
    ('f1000001-0000-4000-8000-000000000033', 'f1000001-0000-4000-8000-000000000002', 'L1', 'ROLE', 'CHECKER', 'ENTITY'),
    ('f1000001-0000-4000-8000-000000000034', 'f1000001-0000-4000-8000-000000000002', 'L2', 'ROLE', 'ADMIN', 'TENANT')
ON CONFLICT (id) DO NOTHING;
