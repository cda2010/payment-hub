-- Coris Money (FSE 0011) : interface utilisateur CONFIRM_PAYMENT
-- Requis après ENTER_OTP (checkout OTP) — sinon INTERFACE_NOT_FOUND / 404.

INSERT INTO interface_utilisateur (
    id, provider_id, code_fse, service_type, type_methode, type_envoi,
    provider_operation_id, provider_api_id, code_interface, nom_interface, actif, created_at
)
SELECT gen_random_uuid(),
       p.id,
       '0011',
       'PAYEMENT',
       'CONFIRM_PAYMENT',
       'REQUEST',
       o.id,
       a.id,
       'CONFIRM_PAYMENT',
       COALESCE(p.libelle, 'CORIS_MONEY') || ' / CONFIRM_PAYMENT',
       TRUE,
       NOW()
FROM payment_provider p
JOIN provider_operation o ON o.provider_id = p.id AND UPPER(o.code) = 'CONFIRM_PAYMENT'
JOIN provider_api a ON a.provider_id = p.id AND UPPER(a.code) = 'CONFIRM_PAYMENT'
WHERE p.code = 'CORIS_MONEY'
  AND NOT EXISTS (
      SELECT 1 FROM interface_utilisateur i
      WHERE i.code_fse = '0011'
        AND i.service_type = 'PAYEMENT'
        AND i.type_methode = 'CONFIRM_PAYMENT'
        AND i.type_envoi = 'REQUEST'
  );

INSERT INTO parametre_utilisateur (
    id, interface_utilisateur_id, nom_parametre, code_parametre, code_parametre_methode,
    type_parametre, attribut, valeur, ordre
)
SELECT gen_random_uuid(), i.id, v.nom, v.code, v.code_methode, 'STRING', v.attribut, v.valeur, v.ordre
FROM interface_utilisateur i
JOIN payment_provider p ON p.id = i.provider_id AND p.code = 'CORIS_MONEY'
CROSS JOIN (VALUES
    ('telephone', 'telephone', NULL, NULL, NULL, 1),
    ('msisdn', 'msisdn', NULL, NULL, NULL, 2),
    ('phone', 'phone', NULL, NULL, NULL, 3),
    ('otp', 'otp', NULL, NULL, NULL, 4),
    ('amount', 'amount', 'MONTANT', NULL, NULL, 5),
    ('transactionAmount', 'transactionAmount', 'MONTANT', NULL, NULL, 6),
    ('currency', 'currency', NULL, NULL, NULL, 7),
    ('currencyCode', 'currencyCode', NULL, NULL, NULL, 8),
    ('codeFse', 'codeFse', NULL, NULL, '0011', 9),
    ('paymentId', 'paymentId', NULL, NULL, NULL, 10)
) AS v(nom, code, code_methode, attribut, valeur, ordre)
WHERE i.code_fse = '0011' AND i.type_methode = 'CONFIRM_PAYMENT' AND i.type_envoi = 'REQUEST'
  AND NOT EXISTS (
      SELECT 1 FROM parametre_utilisateur pu
      WHERE pu.interface_utilisateur_id = i.id AND pu.code_parametre = v.code
  );
