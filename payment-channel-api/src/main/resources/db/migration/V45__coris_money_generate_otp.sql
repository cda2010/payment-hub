-- Coris Money GENERATE_OTP → connecteur /api/gateway/otp (FSE 0011)
-- Aligné sur coris-money-connector POST /api/gateway/otp (+ alias /payments/otp).

UPDATE provider_api a
SET path = 'api/gateway/otp',
    http_method = 'POST',
    content_type = 'application/x-www-form-urlencoded',
    request_mapping_json = '{"telephone":"$${phone}","msisdn":"$${phone}","codeFse":"$${codeFse}","amount":"$${amount}","currency":"$${currency}"}',
    nom = COALESCE(NULLIF(a.nom, ''), 'api/gateway/otp')
FROM payment_provider p
WHERE a.provider_id = p.id
  AND p.code = 'CORIS_MONEY'
  AND UPPER(a.code) = 'GENERATE_OTP';

-- Si l'opération checkout existe déjà mais pas d'endpoint (cas rare)
INSERT INTO provider_operation (id, provider_id, code, nom, description, idempotent, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'GENERATE_OTP',
       'Générer OTP',
       'Omnicanal FSE 0011 · GENERATE_OTP (000707)',
       TRUE,
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'CORIS_MONEY'
  AND NOT EXISTS (
      SELECT 1 FROM provider_operation o
      WHERE o.provider_id = p.id AND UPPER(o.code) = 'GENERATE_OTP'
  );

-- Endpoint Omnicanal lié à l'Api FSE si GENERATE_OTP API absente mais Api 0007 présente
INSERT INTO provider_api (
    id, provider_id, environment_id, fse_api_id, operation_id,
    code, nom, http_method, path, content_type, timeout_seconds,
    request_mapping_json, retry_enabled, max_attempts, retry_delay_ms, backoff_multiplier, actif, created_at
)
SELECT gen_random_uuid(),
       p.id,
       NULL,
       fa.id,
       o.id,
       'GENERATE_OTP',
       'api/gateway/otp',
       'POST',
       'api/gateway/otp',
       'application/x-www-form-urlencoded',
       30,
       '{"telephone":"$${telephone}","msisdn":"$${msisdn}","phone":"$${phone}","codeFse":"$${codeFse}","amount":"$${amount}","currency":"$${currency}"}',
       FALSE,
       1,
       2000,
       1.0,
       TRUE,
       NOW()
FROM payment_provider p
JOIN provider_operation o ON o.provider_id = p.id AND UPPER(o.code) = 'GENERATE_OTP'
JOIN provider_fse_api fa ON fa.provider_id = p.id AND fa.code = '0007'
WHERE p.code = 'CORIS_MONEY'
  AND NOT EXISTS (
      SELECT 1 FROM provider_api a
      WHERE a.provider_id = p.id AND UPPER(a.code) = 'GENERATE_OTP'
  );
