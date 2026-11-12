-- CORIS_MONEY : supporter TRANSFERT (ACCOUNT_TRANSFER catalogue Coris → Coris)
INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'CORIS_MONEY'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'TRANSFERT',
       'Transfert de compte (Coris Money)',
       'TRANSFERT',
       'accountTransfer',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'CORIS_MONEY'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'accountTransfer',
    nom = COALESCE(f.nom, 'Transfert de compte (Coris Money)')
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'CORIS_MONEY'
  AND f.service_type = 'TRANSFERT'
  AND (f.bpm_process_key IS NULL OR f.bpm_process_key IN ('providerWalletTransfer', 'DEFAULT'));

-- Alias catalogue EXECUTE_TRANSFER → même endpoint que TRANSFERT
INSERT INTO provider_operation (id, provider_id, code, nom, description, idempotent, actif, created_at)
SELECT gen_random_uuid(), p.id, 'TRANSFER_EXECUTE', 'Exécuter transfert',
       'Alias TRANSFERT (ACCOUNT_TRANSFER)', FALSE, TRUE, NOW()
FROM payment_provider p
WHERE p.code = 'CORIS_MONEY'
  AND NOT EXISTS (
      SELECT 1 FROM provider_operation o WHERE o.provider_id = p.id AND o.code = 'TRANSFER_EXECUTE'
  );

INSERT INTO provider_api (
    id, provider_id, environment_id, fse_api_id, operation_id,
    code, nom, http_method, path, content_type, timeout_seconds,
    request_mapping_json, retry_enabled, max_attempts, retry_delay_ms, backoff_multiplier, actif, created_at
)
SELECT gen_random_uuid(),
       p.id,
       a.environment_id,
       a.fse_api_id,
       o_new.id,
       'TRANSFER_EXECUTE',
       COALESCE(a.nom, 'api/gateway/virements'),
       a.http_method,
       a.path,
       a.content_type,
       COALESCE(a.timeout_seconds, 30),
       COALESCE(
           a.request_mapping_json,
           '{"compteDebiteur":"$${sourceAccount}","compteCredit":"$${destAccount}","codeFseDebit":"$${codeFse}","codeFseCredit":"$${codeFse}","montant":"$${amount}","reference":"$${customerReference}","description":"ACCOUNT_TRANSFER"}'
       ),
       FALSE, 1, 2000, 1.0, TRUE, NOW()
FROM payment_provider p
JOIN provider_operation o_tr ON o_tr.provider_id = p.id AND o_tr.code = 'TRANSFERT'
JOIN provider_api a ON a.operation_id = o_tr.id
JOIN provider_operation o_new ON o_new.provider_id = p.id AND o_new.code = 'TRANSFER_EXECUTE'
WHERE p.code = 'CORIS_MONEY'
  AND NOT EXISTS (
      SELECT 1 FROM provider_api x WHERE x.provider_id = p.id AND x.code = 'TRANSFER_EXECUTE'
  )
LIMIT 1;
