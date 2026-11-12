-- Moteur Pricing — Frais · Taux · Limites

CREATE TABLE IF NOT EXISTS pricing_fee_schedule (
    id          UUID PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    currency    VARCHAR(10)  NOT NULL DEFAULT 'XOF',
    fee_bearer  VARCHAR(20)  NOT NULL DEFAULT 'SENDER',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pricing_fee_line (
    id            UUID PRIMARY KEY,
    schedule_id   UUID         NOT NULL REFERENCES pricing_fee_schedule (id) ON DELETE CASCADE,
    calc_type     VARCHAR(32)  NOT NULL,
    min_amount    NUMERIC(18,2),
    max_amount    NUMERIC(18,2),
    fixed_amount  NUMERIC(18,2),
    percent_value NUMERIC(18,6),
    min_fee       NUMERIC(18,2),
    max_fee       NUMERIC(18,2),
    ordre         INT          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_pricing_fee_line_schedule ON pricing_fee_line (schedule_id, ordre);

CREATE TABLE IF NOT EXISTS pricing_rate (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    rate_kind       VARCHAR(16)  NOT NULL DEFAULT 'FX',
    base_currency   VARCHAR(10)  NOT NULL,
    quote_currency  VARCHAR(10)  NOT NULL,
    rate            NUMERIC(18,8) NOT NULL,
    valid_from      TIMESTAMPTZ,
    valid_to        TIMESTAMPTZ,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pricing_rate_pair ON pricing_rate (base_currency, quote_currency, active);

CREATE TABLE IF NOT EXISTS pricing_limit (
    id               UUID PRIMARY KEY,
    code             VARCHAR(64)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    period           VARCHAR(16)  NOT NULL DEFAULT 'PER_TXN',
    min_amount       NUMERIC(18,2),
    max_amount       NUMERIC(18,2),
    max_cumulative   NUMERIC(18,2),
    max_count        INT,
    currency         VARCHAR(10)  NOT NULL DEFAULT 'XOF',
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pricing_policy (
    id                 UUID PRIMARY KEY,
    code               VARCHAR(64)  NOT NULL UNIQUE,
    name               VARCHAR(150) NOT NULL,
    product_code       VARCHAR(80),
    channel            VARCHAR(32),
    audience           VARCHAR(32),
    country_code       VARCHAR(8),
    rail_code          VARCHAR(40),
    fee_schedule_code  VARCHAR(64),
    rate_code          VARCHAR(64),
    limit_code         VARCHAR(64),
    priority           INT          NOT NULL DEFAULT 100,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pricing_policy_product ON pricing_policy (product_code, active);

-- ── Seed ───────────────────────────────────────────────────────────────────

INSERT INTO pricing_fee_schedule (id, code, name, currency, fee_bearer, active)
SELECT v.id::uuid, v.code, v.name, 'XOF', v.bearer, TRUE
FROM (VALUES
    ('a1000001-0000-4000-8000-000000000001', 'FEE-DOMESTIC', 'Frais transfert domestique', 'SENDER'),
    ('a1000001-0000-4000-8000-000000000002', 'FEE-RTGS', 'Frais RTGS', 'SENDER'),
    ('a1000001-0000-4000-8000-000000000003', 'FEE-SWIFT', 'Frais SWIFT', 'SENDER'),
    ('a1000001-0000-4000-8000-000000000004', 'FEE-BILL', 'Frais paiement facture', 'SENDER'),
    ('a1000001-0000-4000-8000-000000000005', 'FEE-CASH-AGENT', 'Frais dépôt/retrait agent', 'SENDER')
) AS v(id, code, name, bearer)
WHERE NOT EXISTS (SELECT 1 FROM pricing_fee_schedule LIMIT 1);

INSERT INTO pricing_fee_line (id, schedule_id, calc_type, min_amount, max_amount, fixed_amount, percent_value, min_fee, max_fee, ordre)
SELECT v.id::uuid, v.schedule_id::uuid, v.calc_type, v.min_a, v.max_a, v.fixed_a, v.pct, v.min_f, v.max_f, v.ord
FROM (VALUES
    ('a2000001-0000-4000-8000-000000000001', 'a1000001-0000-4000-8000-000000000001', 'FIXED', 0, 50000, 100, NULL, NULL, NULL, 0),
    ('a2000001-0000-4000-8000-000000000002', 'a1000001-0000-4000-8000-000000000001', 'FIXED_PLUS_PERCENT', 50000.01, 500000, 200, 0.5, 200, 2500, 1),
    ('a2000001-0000-4000-8000-000000000003', 'a1000001-0000-4000-8000-000000000001', 'PERCENT', 500000.01, NULL, NULL, 0.35, 500, 15000, 2),
    ('a2000001-0000-4000-8000-000000000004', 'a1000001-0000-4000-8000-000000000002', 'FIXED', 0, NULL, 5000, NULL, NULL, NULL, 0),
    ('a2000001-0000-4000-8000-000000000005', 'a1000001-0000-4000-8000-000000000003', 'FIXED_PLUS_PERCENT', 0, NULL, 25000, 0.15, 25000, 150000, 0),
    ('a2000001-0000-4000-8000-000000000006', 'a1000001-0000-4000-8000-000000000004', 'FIXED', 0, NULL, 50, NULL, NULL, NULL, 0),
    ('a2000001-0000-4000-8000-000000000007', 'a1000001-0000-4000-8000-000000000005', 'PERCENT', 0, NULL, NULL, 1.0, 100, 5000, 0)
) AS v(id, schedule_id, calc_type, min_a, max_a, fixed_a, pct, min_f, max_f, ord)
WHERE NOT EXISTS (SELECT 1 FROM pricing_fee_line LIMIT 1);

INSERT INTO pricing_rate (id, code, rate_kind, base_currency, quote_currency, rate, active)
SELECT v.id::uuid, v.code, 'FX', v.base, v.quote, v.rate, TRUE
FROM (VALUES
    ('a3000001-0000-4000-8000-000000000001', 'FX-EUR-XOF', 'EUR', 'XOF', 655.95700000),
    ('a3000001-0000-4000-8000-000000000002', 'FX-USD-XOF', 'USD', 'XOF', 605.00000000),
    ('a3000001-0000-4000-8000-000000000003', 'FX-EUR-USD', 'EUR', 'USD', 1.08500000)
) AS v(id, code, base, quote, rate)
WHERE NOT EXISTS (SELECT 1 FROM pricing_rate LIMIT 1);

INSERT INTO pricing_limit (id, code, name, period, min_amount, max_amount, max_cumulative, max_count, currency, active)
SELECT v.id::uuid, v.code, v.name, v.period, v.min_a, v.max_a, v.max_c, v.max_n, 'XOF', TRUE
FROM (VALUES
    ('a4000001-0000-4000-8000-000000000001', 'LIM-RETAIL-TXN', 'Limite retail par opération', 'PER_TXN', 100, 2000000, NULL, NULL),
    ('a4000001-0000-4000-8000-000000000002', 'LIM-RETAIL-DAY', 'Limite retail journalière', 'DAILY', 100, 2000000, 5000000, 20),
    ('a4000001-0000-4000-8000-000000000003', 'LIM-CORP-TXN', 'Limite corporate par opération', 'PER_TXN', 1000, 500000000, NULL, NULL),
    ('a4000001-0000-4000-8000-000000000004', 'LIM-RTGS', 'Limite RTGS', 'PER_TXN', 100000, 1000000000, NULL, NULL),
    ('a4000001-0000-4000-8000-000000000005', 'LIM-AGENT-CASH', 'Limite caisse agent', 'PER_TXN', 500, 1500000, NULL, NULL),
    ('a4000001-0000-4000-8000-000000000006', 'LIM-AGENT-DAY', 'Limite agent journalière', 'DAILY', 500, 1500000, 10000000, 50)
) AS v(id, code, name, period, min_a, max_a, max_c, max_n)
WHERE NOT EXISTS (SELECT 1 FROM pricing_limit LIMIT 1);

INSERT INTO pricing_policy (
    id, code, name, product_code, channel, audience, country_code, rail_code,
    fee_schedule_code, rate_code, limit_code, priority, active
)
SELECT v.id::uuid, v.code, v.name, v.product, v.channel, v.audience, v.country, v.rail,
       v.fee, v.rate, v.lim, v.prio, TRUE
FROM (VALUES
    ('a5000001-0000-4000-8000-000000000001', 'POL-TRANSFER-RETAIL', 'Transfert retail WEB',
     'ACCOUNT_TRANSFER', 'WEB', 'RETAIL', 'BF', NULL, 'FEE-DOMESTIC', NULL, 'LIM-RETAIL-TXN', 200),
    ('a5000001-0000-4000-8000-000000000002', 'POL-TRANSFER-CORP', 'Transfert corporate WEB',
     'ACCOUNT_TRANSFER', 'WEB', 'CORPORATE', NULL, NULL, 'FEE-DOMESTIC', NULL, 'LIM-CORP-TXN', 200),
    ('a5000001-0000-4000-8000-000000000003', 'POL-RTGS', 'RTGS corporate',
     'RTGS', 'WEB', 'CORPORATE', NULL, 'RTGS', 'FEE-RTGS', NULL, 'LIM-RTGS', 300),
    ('a5000001-0000-4000-8000-000000000004', 'POL-SWIFT', 'SWIFT corporate',
     'SWIFT', 'WEB', 'CORPORATE', NULL, 'SWIFT', 'FEE-SWIFT', 'FX-EUR-XOF', 'LIM-CORP-TXN', 300),
    ('a5000001-0000-4000-8000-000000000005', 'POL-BILL', 'Paiement facture',
     'BILL_PAYMENT', NULL, NULL, NULL, NULL, 'FEE-BILL', NULL, 'LIM-RETAIL-TXN', 100),
    ('a5000001-0000-4000-8000-000000000006', 'POL-CASH-AGENT', 'Dépôt/retrait agent',
     'CASH_IN', NULL, 'AGENT', NULL, NULL, 'FEE-CASH-AGENT', NULL, 'LIM-AGENT-CASH', 200)
) AS v(id, code, name, product, channel, audience, country, rail, fee, rate, lim, prio)
WHERE NOT EXISTS (SELECT 1 FROM pricing_policy LIMIT 1);
