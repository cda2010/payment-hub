-- Coris Bank GENERATE_OTP → connecteur /api/v1/otp (FSE 0004 / Api 0001)
-- Aligné sur cbibf-connector POST /api/v1/otp (+ alias /api/gateway/otp, /payments/otp).

INSERT INTO provider_operation (id, provider_id, code, nom, description, idempotent, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'GENERATE_OTP',
       'Générer OTP',
       'Omnicanal FSE 0004 · GENERATE_OTP (000119)',
       TRUE,
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'CORIS_BANK'
  AND NOT EXISTS (
      SELECT 1 FROM provider_operation o
      WHERE o.provider_id = p.id AND UPPER(o.code) = 'GENERATE_OTP'
  );

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
       'api/v1/otp',
       'POST',
       'api/v1/otp',
       'application/json',
       30,
       '{"telephone":"$${telephone}","msisdn":"$${msisdn}","phone":"$${phone}","numeroCompte":"$${numeroCompte}","compteDebiteur":"$${compteDebiteur}","codeFse":"$${codeFse}","amount":"$${amount}","currency":"$${currency}"}',
       FALSE,
       1,
       2000,
       1.0,
       TRUE,
       NOW()
FROM payment_provider p
JOIN provider_operation o ON o.provider_id = p.id AND UPPER(o.code) = 'GENERATE_OTP'
JOIN provider_fse_api fa ON fa.provider_id = p.id AND fa.code = '0001'
WHERE p.code = 'CORIS_BANK'
  AND NOT EXISTS (
      SELECT 1 FROM provider_api a
      WHERE a.provider_id = p.id AND UPPER(a.code) = 'GENERATE_OTP'
  );

UPDATE provider_api a
SET path = 'api/v1/otp',
    http_method = 'POST',
    content_type = 'application/json',
    request_mapping_json = '{"telephone":"$${telephone}","msisdn":"$${msisdn}","phone":"$${phone}","numeroCompte":"$${numeroCompte}","compteDebiteur":"$${compteDebiteur}","codeFse":"$${codeFse}","amount":"$${amount}","currency":"$${currency}"}',
    nom = COALESCE(NULLIF(a.nom, ''), 'api/v1/otp'),
    fse_api_id = COALESCE(a.fse_api_id, fa.id)
FROM payment_provider p
JOIN provider_fse_api fa ON fa.provider_id = p.id AND fa.code = '0001'
WHERE a.provider_id = p.id
  AND p.code = 'CORIS_BANK'
  AND UPPER(a.code) = 'GENERATE_OTP';

INSERT INTO provider_otp_config (
    id, provider_id, otp_required, length, validity_seconds, max_attempts,
    resend_allowed, resend_delay_seconds, generate_operation, validate_operation
)
SELECT gen_random_uuid(),
       p.id,
       TRUE,
       6,
       120,
       3,
       TRUE,
       30,
       'GENERATE_OTP',
       'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'CORIS_BANK'
  AND NOT EXISTS (
      SELECT 1 FROM provider_otp_config c WHERE c.provider_id = p.id
  );

-- Interface utilisateur + paramètres (orchestration ACCOUNT_TRANSFER / GENERATE_OTP)
INSERT INTO interface_utilisateur (
    id, provider_id, code_fse, service_type, type_methode, type_envoi,
    provider_operation_id, provider_api_id, code_interface, nom_interface, actif, created_at
)
SELECT gen_random_uuid(),
       p.id,
       '0004',
       'PAYEMENT',
       'GENERATE_OTP',
       'REQUEST',
       o.id,
       a.id,
       'GENERATE_OTP',
       COALESCE(p.libelle, 'CORIS_BANK') || ' / GENERATE_OTP',
       TRUE,
       NOW()
FROM payment_provider p
JOIN provider_operation o ON o.provider_id = p.id AND UPPER(o.code) = 'GENERATE_OTP'
JOIN provider_api a ON a.provider_id = p.id AND UPPER(a.code) = 'GENERATE_OTP'
WHERE p.code = 'CORIS_BANK'
  AND NOT EXISTS (
      SELECT 1 FROM interface_utilisateur i
      WHERE i.code_fse = '0004'
        AND i.service_type = 'PAYEMENT'
        AND i.type_methode = 'GENERATE_OTP'
        AND i.type_envoi = 'REQUEST'
  );

INSERT INTO parametre_utilisateur (
    id, interface_utilisateur_id, nom_parametre, code_parametre, code_parametre_methode,
    type_parametre, attribut, valeur, ordre
)
SELECT gen_random_uuid(), i.id, v.nom, v.code, v.code_methode, 'STRING', v.attribut, v.valeur, v.ordre
FROM interface_utilisateur i
JOIN payment_provider p ON p.id = i.provider_id AND p.code = 'CORIS_BANK'
CROSS JOIN (VALUES
    ('telephone', 'telephone', NULL, NULL, NULL, 1),
    ('msisdn', 'msisdn', NULL, NULL, NULL, 2),
    ('phone', 'phone', NULL, NULL, NULL, 3),
    ('numeroCompte', 'numeroCompte', 'CPTSOURCE', 'NUMERO_COMPTE', NULL, 4),
    ('compteDebiteur', 'compteDebiteur', 'CPTSOURCE', 'NUMERO_COMPTE', NULL, 5),
    ('codeFse', 'codeFse', NULL, NULL, '0004', 6),
    ('amount', 'amount', 'MONTANT', NULL, NULL, 7),
    ('currency', 'currency', NULL, NULL, NULL, 8)
) AS v(nom, code, code_methode, attribut, valeur, ordre)
WHERE i.code_fse = '0004' AND i.type_methode = 'GENERATE_OTP' AND i.type_envoi = 'REQUEST'
  AND NOT EXISTS (
      SELECT 1 FROM parametre_utilisateur pu
      WHERE pu.interface_utilisateur_id = i.id AND pu.code_parametre = v.code
  );
