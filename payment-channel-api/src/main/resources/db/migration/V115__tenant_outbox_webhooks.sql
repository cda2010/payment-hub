-- Lot transversal : multi-tenant txn + outbox événements + webhooks sortants.

ALTER TABLE payment_transaction
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_payment_transaction_tenant
    ON payment_transaction (tenant_id);

CREATE TABLE IF NOT EXISTS domain_outbox (
    id              UUID PRIMARY KEY,
    event_id        VARCHAR(64)  NOT NULL UNIQUE,
    event_type      VARCHAR(80)  NOT NULL,
    aggregate_type  VARCHAR(40)  NOT NULL,
    aggregate_id    VARCHAR(80)  NOT NULL,
    tenant_id       VARCHAR(64),
    payload_json    TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempts        INT          NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_error      VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_domain_outbox_pending
    ON domain_outbox (status, next_attempt_at);

CREATE TABLE IF NOT EXISTS webhook_subscription (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    code            VARCHAR(64)  NOT NULL,
    target_url      VARCHAR(500) NOT NULL,
    secret_enc      TEXT,
    event_types     VARCHAR(500) NOT NULL DEFAULT '*',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_webhook_subscription_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_webhook_subscription_tenant
    ON webhook_subscription (tenant_id, active);
