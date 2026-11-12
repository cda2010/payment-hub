-- Inbox « À faire » : déplacée depuis payment_gateway (V129 / V130).

CREATE TABLE IF NOT EXISTS party_action (
    id               UUID PRIMARY KEY,
    tenant_id        VARCHAR(64),
    party_id         UUID         NOT NULL,
    action_type      VARCHAR(64)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    title            VARCHAR(255) NOT NULL,
    subtitle         VARCHAR(512),
    icon             VARCHAR(64),
    route            VARCHAR(255),
    priority         INT          NOT NULL DEFAULT 100,
    aggregate_type   VARCHAR(40),
    aggregate_id     VARCHAR(80),
    source_event_id  VARCHAR(64),
    payload_json     TEXT,
    expires_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_party_action_party_status
    ON party_action (party_id, status, priority ASC, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_party_action_type
    ON party_action (action_type, status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_party_action_open_aggregate
    ON party_action (
        party_id,
        action_type,
        COALESCE(aggregate_type, ''),
        COALESCE(aggregate_id, '')
    )
    WHERE status = 'OPEN';

-- Jeu de démonstration : party retail Awa (PERS-RETAIL1).
INSERT INTO party_action (
    id, tenant_id, party_id, action_type, status,
    title, subtitle, icon, route, priority,
    aggregate_type, aggregate_id, source_event_id,
    expires_at, created_at, updated_at
)
SELECT v.id::uuid, 'CORIS_DEMO', 'a1000001-0000-4000-8000-000000000006'::uuid,
       v.action_type, 'OPEN',
       v.title, v.subtitle, v.icon, v.route, v.priority,
       v.aggregate_type, v.aggregate_id, v.source_event_id,
       v.expires_at::timestamptz, NOW(), NOW()
FROM (VALUES
    (
        'f1000001-0000-4000-8000-000000000001',
        'FIRST_OP',
        'Tutoriel première opération',
        'Guide rapide transfert ou facture',
        'school-outline',
        '/tabs/transfer',
        10,
        'party',
        'a1000001-0000-4000-8000-000000000006',
        'SEED-FIRST-OP-AWA',
        NULL
    ),
    (
        'f1000001-0000-4000-8000-000000000002',
        'KYC_RENEW',
        'Pièce bientôt expirée',
        'Mettez à jour votre dossier KYC',
        'id-card-outline',
        '/kyc',
        20,
        'party',
        'a1000001-0000-4000-8000-000000000006',
        'SEED-KYC-AWA',
        (NOW() + INTERVAL '30 days')::text
    ),
    (
        'f1000001-0000-4000-8000-000000000003',
        'COTISATION_DUE',
        '2 cotisations à régler',
        'Associations et clubs',
        'people-circle-outline',
        '/cotisations',
        30,
        'cotisation',
        'SEED-COTIS-BATCH',
        'SEED-COTIS-AWA',
        (NOW() + INTERVAL '14 days')::text
    ),
    (
        'f1000001-0000-4000-8000-000000000004',
        'CASHOUT_CODE',
        '1 code retrait actif',
        'À utiliser avant expiration',
        'keypad-outline',
        '/retrait-code-active',
        40,
        'voucher',
        'SEED-VOUCHER-AWA-1',
        'SEED-CASHOUT-AWA',
        (NOW() + INTERVAL '2 days')::text
    )
) AS v(id, action_type, title, subtitle, icon, route, priority, aggregate_type, aggregate_id, source_event_id, expires_at)
WHERE NOT EXISTS (
    SELECT 1 FROM party_action
    WHERE party_id = 'a1000001-0000-4000-8000-000000000006'::uuid
      AND action_type = v.action_type
      AND status = 'OPEN'
);
