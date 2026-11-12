-- Inbox « À faire » : actions ouvertes par party (nourries par les domaines métier).

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

-- Une seule action OPEN par (party, type, agrégat)
CREATE UNIQUE INDEX IF NOT EXISTS uq_party_action_open_aggregate
    ON party_action (
        party_id,
        action_type,
        COALESCE(aggregate_type, ''),
        COALESCE(aggregate_id, '')
    )
    WHERE status = 'OPEN';
