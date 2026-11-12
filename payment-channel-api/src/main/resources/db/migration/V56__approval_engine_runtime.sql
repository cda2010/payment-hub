-- Approbation générique : profiles + instances runtime + historique
CREATE TABLE approval_profile (
    id               UUID PRIMARY KEY,
    code             VARCHAR(80)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    definition_json  TEXT         NOT NULL,
    actif            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE approval_instance (
    id                     UUID PRIMARY KEY,
    payment_id             VARCHAR(64)  NOT NULL UNIQUE,
    policy_code            VARCHAR(80),
    profile_code           VARCHAR(80)  NOT NULL,
    strategy               VARCHAR(40)  NOT NULL DEFAULT 'SEQUENTIAL',
    status                 VARCHAR(40)  NOT NULL,
    current_level          INT,
    total_levels           INT          NOT NULL DEFAULT 0,
    policy_snapshot_json   TEXT,
    profile_snapshot_json  TEXT,
    context_fingerprint    VARCHAR(128),
    initiated_by           VARCHAR(120),
    dmn_decision_key       VARCHAR(120),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at           TIMESTAMPTZ
);

CREATE INDEX idx_approval_instance_status ON approval_instance (status);

CREATE TABLE approval_step_instance (
    id                       UUID PRIMARY KEY,
    approval_instance_id     UUID         NOT NULL REFERENCES approval_instance(id) ON DELETE CASCADE,
    level_number             INT          NOT NULL,
    required_approvals       INT          NOT NULL DEFAULT 1,
    collected_approvals      INT          NOT NULL DEFAULT 0,
    status                   VARCHAR(40)  NOT NULL,
    assignments_json         TEXT,
    candidate_groups         VARCHAR(500),
    bpm_task_definition_key  VARCHAR(80),
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at             TIMESTAMPTZ,
    UNIQUE (approval_instance_id, level_number)
);

CREATE INDEX idx_approval_step_instance_status ON approval_step_instance (status);

CREATE TABLE approval_decision (
    id                         UUID PRIMARY KEY,
    approval_step_instance_id  UUID         NOT NULL REFERENCES approval_step_instance(id) ON DELETE CASCADE,
    approver_id                VARCHAR(120) NOT NULL,
    approver_role              VARCHAR(120),
    outcome                    VARCHAR(20)  NOT NULL,
    comment                    VARCHAR(1000),
    decided_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_approval_decision_step ON approval_decision (approval_step_instance_id);

-- Profils seed (DMN retourne le code)
INSERT INTO approval_profile (id, code, name, description, definition_json, actif, created_at, updated_at)
VALUES
(
    'a1000000-0000-4000-8000-000000000001',
    'NO_APPROVAL',
    'Aucune approbation',
    'Circuit sans validation humaine',
    '{"strategy":"SEQUENTIAL","levels":[]}',
    TRUE, NOW(), NOW()
),
(
    'a1000000-0000-4000-8000-000000000002',
    'LEVEL_1',
    'Niveau 1 — Chef d''agence',
    '1 M – 10 M XOF',
    '{"strategy":"SEQUENTIAL","levels":[{"level":1,"requiredApprovals":1,"assignments":[{"type":"ROLE","value":"CHEF_AGENCE"}]}]}',
    TRUE, NOW(), NOW()
),
(
    'a1000000-0000-4000-8000-000000000003',
    'LEVEL_1_2',
    'Niveaux 1+2',
    '10 M – 50 M XOF',
    '{"strategy":"SEQUENTIAL","levels":[{"level":1,"requiredApprovals":1,"assignments":[{"type":"ROLE","value":"CHEF_AGENCE"}]},{"level":2,"requiredApprovals":1,"assignments":[{"type":"ROLE","value":"RESPONSABLE_OPERATIONS"}]}]}',
    TRUE, NOW(), NOW()
),
(
    'a1000000-0000-4000-8000-000000000004',
    'HIGH_VALUE',
    'Haut montant',
    '> 50 M XOF — 3 niveaux',
    '{"strategy":"SEQUENTIAL","levels":[{"level":1,"requiredApprovals":1,"assignments":[{"type":"ROLE","value":"CHEF_AGENCE"}]},{"level":2,"requiredApprovals":2,"assignments":[{"type":"ROLE","value":"RESPONSABLE_OPERATIONS"}]},{"level":3,"requiredApprovals":1,"assignments":[{"type":"ROLE","value":"DIRECTEUR"}]}]}',
    TRUE, NOW(), NOW()
);

-- Policy catalogue : DMN → profileCode (plus de levels hardcodés)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{approvalPolicy}',
        jsonb_build_object(
                'code', 'TRANSFER_APPROVAL',
                'enabled', true,
                'strategy', 'SEQUENTIAL',
                'dmnDecisionKey', 'decision_TransferApproval',
                'initiatorCanApprove', false,
                'sameUserAcrossLevelsAllowed', false,
                'formKey', 'account-transfer-approval',
                'criticalFields', jsonb_build_array('amount', 'currency', 'destAccount', 'sourceAccount', 'beneficiaryAccount')
        ),
        true
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';
