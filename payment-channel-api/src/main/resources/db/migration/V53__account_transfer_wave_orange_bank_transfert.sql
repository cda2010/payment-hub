-- WAVE / ORANGE_MONEY : supporter TRANSFERT (ACCOUNT_TRANSFER catalogue)
-- CORIS_BANK : brancher le flow TRANSFERT sur le BPMN accountTransfer

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code IN ('WAVE', 'ORANGE_MONEY')
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'TRANSFERT',
       CASE p.code
           WHEN 'WAVE' THEN 'Transfert de compte (Wave)'
           WHEN 'ORANGE_MONEY' THEN 'Transfert de compte (Orange Money)'
           ELSE 'Transfert de compte'
       END,
       'TRANSFERT',
       'accountTransfer',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code IN ('WAVE', 'ORANGE_MONEY')
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'accountTransfer',
    nom = CASE p.code
             WHEN 'CORIS_BANK' THEN 'Transfert de compte (Coris Bank)'
             WHEN 'WAVE' THEN 'Transfert de compte (Wave)'
             WHEN 'ORANGE_MONEY' THEN 'Transfert de compte (Orange Money)'
             ELSE COALESCE(f.nom, 'Transfert de compte')
         END
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code IN ('CORIS_BANK', 'WAVE', 'ORANGE_MONEY')
  AND f.service_type = 'TRANSFERT'
  AND (f.bpm_process_key IS NULL OR f.bpm_process_key IN ('providerWalletTransfer', 'providerBankTransfer', 'DEFAULT'));
