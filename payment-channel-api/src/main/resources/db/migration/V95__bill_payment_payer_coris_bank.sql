-- BILL_PAYMENT / CHECKOUT : autoriser CORIS_BANK comme PAYER
-- (en plus des wallets), pour pouvoir changer de moyen de paiement
-- côté mobile (portefeuille ↔ compte courant), comme cash-out SOURCE.

-- 1) Participants PAYER
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{participants}',
        COALESCE(v.configuration -> 'participants', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object('role', 'PAYER', 'providerCode', 'CORIS_BANK', 'default', false)
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code IN ('BILL_PAYMENT', 'CHECKOUT')
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'participants', '[]'::jsonb)) p
        WHERE p ->> 'providerCode' = 'CORIS_BANK'
          AND UPPER(p ->> 'role') = 'PAYER'
    );

-- 2) CORIS_BANK doit être un provider PAYEMENT pour l'enrichissement
--    payProvider et resolveWalletCheckoutProcessKey.
INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAYEMENT'
FROM payment_provider p
WHERE p.code = 'CORIS_BANK'
ON CONFLICT DO NOTHING;

-- 3) Flow PAYEMENT : réutilise le BPMN OTP checkout du wallet Coris Money
--    s'il existe, sinon providerOtpCheckout (GENERATE_OTP déjà branché V47).
INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       bank.id,
       'PAYEMENT',
       'Paiement facture / checkout (Coris Bank)',
       'PAYEMENT',
       COALESCE(
           (SELECT f.bpm_process_key
            FROM payment_flow f
            JOIN payment_provider w ON w.id = f.provider_id
            WHERE w.code = 'CORIS_MONEY'
              AND f.service_type = 'PAYEMENT'
              AND f.actif = TRUE
            LIMIT 1),
           'providerOtpCheckout'
       ),
       TRUE,
       NOW()
FROM payment_provider bank
WHERE bank.code = 'CORIS_BANK'
  AND NOT EXISTS (
        SELECT 1 FROM payment_flow f
        WHERE f.provider_id = bank.id AND f.service_type = 'PAYEMENT'
  );
