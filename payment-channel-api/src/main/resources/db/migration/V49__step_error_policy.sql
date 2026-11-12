-- Policies d'erreur par jambe (miroir Omnicanal step_error_policy, types payment-2).
CREATE TABLE IF NOT EXISTS step_error_policy (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    error_type VARCHAR(48) NOT NULL,
    action VARCHAR(48) NOT NULL,
    max_retries INT,
    retry_delay_ms BIGINT,
    target_step_code VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    schema_ecriture_id UUID
);

CREATE INDEX IF NOT EXISTS idx_step_error_policy_code_type
    ON step_error_policy (code, error_type);

-- DEBIT_SOURCE (jambe débit / source)
INSERT INTO step_error_policy (id, code, error_type, action, max_retries, retry_delay_ms, active)
VALUES
    ('f0000001-0000-4000-8000-000000000001', 'DEBIT_SOURCE', 'BUSINESS_REJECTED', 'STOP_WORKFLOW', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000002', 'DEBIT_SOURCE', 'TRANSIENT', 'STOP_WORKFLOW', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000003', 'DEBIT_SOURCE', 'TIMEOUT', 'MANUAL_REVIEW', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000004', 'DEBIT_SOURCE', 'TECHNICAL', 'STOP_WORKFLOW', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000005', 'DEBIT_SOURCE', 'UNKNOWN', 'MANUAL_REVIEW', 0, NULL, TRUE)
ON CONFLICT (id) DO NOTHING;

-- CREDIT_DESTINATION (jambe crédit / dest) — échec après débit source → compenser
INSERT INTO step_error_policy (id, code, error_type, action, max_retries, retry_delay_ms, active)
VALUES
    ('f0000001-0000-4000-8000-000000000011', 'CREDIT_DESTINATION', 'BUSINESS_REJECTED', 'COMPENSATE', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000012', 'CREDIT_DESTINATION', 'TRANSIENT', 'RETRY', 3, 200, TRUE),
    ('f0000001-0000-4000-8000-000000000013', 'CREDIT_DESTINATION', 'TIMEOUT', 'COMPENSATE', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000014', 'CREDIT_DESTINATION', 'TECHNICAL', 'COMPENSATE', 0, NULL, TRUE),
    ('f0000001-0000-4000-8000-000000000015', 'CREDIT_DESTINATION', 'UNKNOWN', 'MANUAL_REVIEW', 0, NULL, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Taguer les lignes TRANSFERT existantes si error_policy_code vide
UPDATE ligne_schema_ecriture
SET error_policy_code = 'DEBIT_SOURCE',
    criticality = COALESCE(NULLIF(TRIM(criticality), ''), 'CRITICAL')
WHERE (error_policy_code IS NULL OR TRIM(error_policy_code) = '')
  AND (
      LOWER(COALESCE(step_id, '')) LIKE '%source%'
      OR LOWER(COALESCE(step_id, '')) LIKE '%debit%'
      OR api_a_utiliser = 'FSESOURCE'
  );

UPDATE ligne_schema_ecriture
SET error_policy_code = 'CREDIT_DESTINATION',
    criticality = COALESCE(NULLIF(TRIM(criticality), ''), 'CRITICAL')
WHERE (error_policy_code IS NULL OR TRIM(error_policy_code) = '')
  AND (
      LOWER(COALESCE(step_id, '')) LIKE '%dest%'
      OR LOWER(COALESCE(step_id, '')) LIKE '%credit%'
      OR api_a_utiliser IN ('FSEDESTINATAIRE', 'FSEPARTENAIRE')
  );
