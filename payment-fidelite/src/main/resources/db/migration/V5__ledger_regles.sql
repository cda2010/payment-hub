-- Socle du cahier : tenant, ledger concurrent, règle publiée, événement reçu.
-- Les tables françaises déjà en place restent la source du catalogue et des adhésions.

CREATE TABLE tenant (
    id         UUID PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(200) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO tenant (id, code, name, status)
VALUES ('c1000001-0000-4000-8000-0000000000a1', 'CORIS_DEMO', 'Coris', 'ACTIF');

ALTER TABLE programme ADD COLUMN code VARCHAR(50);
ALTER TABLE programme ADD COLUMN unit_code VARCHAR(30) NOT NULL DEFAULT 'POINT';
ALTER TABLE programme ADD COLUMN start_at TIMESTAMPTZ;
ALTER TABLE programme ADD COLUMN end_at TIMESTAMPTZ;

UPDATE programme
SET code = 'CORIS'
WHERE id = 'c1000001-0000-4000-8000-000000000001';

UPDATE programme
SET code = 'PRG-' || substr(id::text, 1, 8)
WHERE code IS NULL;

ALTER TABLE programme ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX programme_tenant_code_uk ON programme (tenant_id, code);

CREATE TABLE loyalty_account (
    id               UUID PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    programme_id     UUID        NOT NULL REFERENCES programme (id),
    party_id         UUID        NOT NULL,
    balance          BIGINT      NOT NULL DEFAULT 0,
    reserved_balance BIGINT      NOT NULL DEFAULT 0,
    version          BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT loyalty_account_balance_ck CHECK (
        balance >= 0 AND reserved_balance >= 0 AND reserved_balance <= balance
    ),
    CONSTRAINT loyalty_account_party_uk UNIQUE (tenant_id, programme_id, party_id)
);

CREATE TABLE point_movement (
    id                 UUID PRIMARY KEY,
    tenant_id          VARCHAR(64)  NOT NULL,
    account_id         UUID         NOT NULL REFERENCES loyalty_account (id),
    business_reference VARCHAR(150) NOT NULL,
    movement_type      VARCHAR(30)  NOT NULL,
    points             BIGINT       NOT NULL,
    motif              VARCHAR(160) NOT NULL,
    rule_version_id    UUID,
    created_at         TIMESTAMPTZ  NOT NULL,
    CONSTRAINT point_movement_points_ck CHECK (points <> 0),
    CONSTRAINT point_movement_reference_uk UNIQUE (tenant_id, business_reference)
);

CREATE INDEX point_movement_account_idx ON point_movement (account_id, created_at DESC);

INSERT INTO loyalty_account (id, tenant_id, programme_id, party_id, balance, reserved_balance, version)
SELECT gen_random_uuid(), p.tenant_id, s.programme_id, s.party_id, GREATEST(s.total, 0), 0, 0
FROM (
    SELECT programme_id, party_id, SUM(points)::bigint AS total
    FROM mouvement_points
    GROUP BY programme_id, party_id
) s
JOIN programme p ON p.id = s.programme_id;

INSERT INTO loyalty_account (id, tenant_id, programme_id, party_id, balance, reserved_balance, version)
SELECT gen_random_uuid(), p.tenant_id, a.programme_id, a.party_id, 0, 0, 0
FROM adhesion a
JOIN programme p ON p.id = a.programme_id
WHERE NOT EXISTS (
    SELECT 1
    FROM loyalty_account acc
    WHERE acc.programme_id = a.programme_id
      AND acc.party_id = a.party_id
      AND acc.tenant_id = p.tenant_id
);

INSERT INTO point_movement (
    id, tenant_id, account_id, business_reference, movement_type, points, motif, created_at
)
SELECT m.id,
       p.tenant_id,
       acc.id,
       'LEGACY:' || m.id::text,
       CASE WHEN m.points > 0 THEN 'CREDIT' ELSE 'DEBIT' END,
       m.points,
       m.motif,
       m.created_at
FROM mouvement_points m
JOIN programme p ON p.id = m.programme_id
JOIN loyalty_account acc
  ON acc.programme_id = m.programme_id
 AND acc.party_id = m.party_id
 AND acc.tenant_id = p.tenant_id
WHERE m.points <> 0;

CREATE TABLE loyalty_rule (
    id           UUID PRIMARY KEY,
    tenant_id    VARCHAR(64) NOT NULL,
    programme_id UUID        NOT NULL REFERENCES programme (id),
    code         VARCHAR(50) NOT NULL,
    statut       VARCHAR(20) NOT NULL,
    CONSTRAINT loyalty_rule_code_uk UNIQUE (tenant_id, code)
);

CREATE TABLE loyalty_rule_version (
    id           UUID PRIMARY KEY,
    rule_id      UUID        NOT NULL REFERENCES loyalty_rule (id),
    version_no   INT         NOT NULL,
    priority     INT         NOT NULL,
    definition   TEXT        NOT NULL,
    published_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT loyalty_rule_version_uk UNIQUE (rule_id, version_no)
);

INSERT INTO loyalty_rule (id, tenant_id, programme_id, code, statut)
SELECT 'c1000001-0000-4000-8000-000000000041', tenant_id, id, 'PAYMENT_MERCHANT', 'PUBLIE'
FROM programme
WHERE id = 'c1000001-0000-4000-8000-000000000001';

INSERT INTO loyalty_rule_version (id, rule_id, version_no, priority, definition, published_at)
SELECT 'c1000001-0000-4000-8000-000000000042',
       'c1000001-0000-4000-8000-000000000041',
       1,
       100,
       '{"eventType":"PAYMENT_COMPLETED","conditions":{"operator":"AND","rules":[{"operator":"GTE","field":"amount","value":5000},{"operator":"EQ","field":"channel","value":"MOBILE"}]},"calculation":{"type":"PROPORTIONAL","unitAmount":1000,"pointsPerUnit":2,"rounding":"FLOOR"},"limits":{"maxPointsPerTransaction":500}}',
       now()
WHERE EXISTS (
    SELECT 1 FROM loyalty_rule WHERE id = 'c1000001-0000-4000-8000-000000000041'
);

CREATE TABLE inbound_event (
    id           UUID PRIMARY KEY,
    tenant_id    VARCHAR(64)  NOT NULL,
    event_id     UUID         NOT NULL,
    event_type   VARCHAR(80)  NOT NULL,
    customer_id  VARCHAR(100) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    received_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT inbound_event_uk UNIQUE (tenant_id, event_id)
);

ALTER TABLE echange ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'FULFILLED';
ALTER TABLE echange ADD COLUMN business_reference VARCHAR(150);
