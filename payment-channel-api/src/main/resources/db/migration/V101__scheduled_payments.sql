-- Paiements programmés (mandats récurrents) + produit catalogue SCHEDULED_PAYMENT.

CREATE TABLE IF NOT EXISTS scheduled_payment (
    id                      UUID PRIMARY KEY,
    label                   VARCHAR(150) NOT NULL,
    product_code            VARCHAR(80) NOT NULL,
    amount                  NUMERIC(18, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'XOF',
    frequency               VARCHAR(30) NOT NULL,
    next_run_at             DATE NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    destination_provider    VARCHAR(50),
    dest_phone              VARCHAR(30),
    dest_account            VARCHAR(64),
    beneficiary_label       VARCHAR(150),
    biller_code             VARCHAR(80),
    subscriber_reference    VARCHAR(120),
    source_rail             VARCHAR(50),
    source_payment_order_id VARCHAR(80),
    payload_json            TEXT,
    owner_key               VARCHAR(80),
    actif                   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_scheduled_payment_actif_status
    ON scheduled_payment (actif, status, next_run_at);

-- Seed démo
INSERT INTO scheduled_payment (
    id, label, product_code, amount, currency, frequency, next_run_at, status,
    destination_provider, dest_phone, beneficiary_label, source_rail, owner_key, actif, created_at, updated_at
)
SELECT gen_random_uuid(), v.label, v.product_code, v.amount, 'XOF', v.frequency, CURRENT_DATE + v.days, 'ACTIVE',
       v.destination_provider, v.dest_phone, v.beneficiary_label, 'CORIS_MONEY', 'demo', TRUE, NOW(), NOW()
FROM (VALUES
          ('Loyer famille', 'ACCOUNT_TRANSFER', 75000.00, 'MONTHLY', 7, 'ORANGE_MONEY', '70000001', 'Awa Ouédraogo'),
          ('Facture SONABEL', 'BILL_PAYMENT', 12500.00, 'MONTHLY', 14, NULL, NULL, 'SONABEL'),
          ('Envoi hebdo', 'ACCOUNT_TRANSFER', 5000.00, 'WEEKLY', 3, 'MOOV_MONEY', '70000002', 'Issa Kaboré')
     ) AS v(label, product_code, amount, frequency, days, destination_provider, dest_phone, beneficiary_label)
WHERE NOT EXISTS (SELECT 1 FROM scheduled_payment LIMIT 1);

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'SCHEDULED_PAYMENT',
       'Mes paiements programmés',
       'Mandats de paiement récurrents',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'BILLING'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'SCHEDULED_PAYMENT');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       NULL,
       jsonb_build_object(
               'serviceType', 'PAYEMENT',
               'journeyCode', 'SCHEDULED_PAYMENT_BOOK',
               'supportedChannels', jsonb_build_array('MOBILE'),
               'defaultChannel', 'MOBILE'
       ),
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'SCHEDULED_PAYMENT'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'SCHEDULED_PAYMENT'
  AND v.version = '1.0';
