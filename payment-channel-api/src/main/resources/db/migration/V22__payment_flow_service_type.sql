-- Chaque PaymentFlow est lié à un type de service (1 couple provider+serviceType = 1 flow)
ALTER TABLE payment_flow
    ADD COLUMN IF NOT EXISTS service_type VARCHAR(40);

-- Backfill depuis le code flow historique
UPDATE payment_flow f
SET service_type = CASE
    WHEN f.code IN ('TRANSFER', 'TRANSFERT') THEN 'TRANSFERT'
    WHEN f.code IN ('BILL_PAY', 'PAIEMENT_FACTURE') THEN 'PAIEMENT_FACTURE'
    WHEN f.code IN ('LINK', 'UNLINK', 'CONSULTATION_SOLDE', 'PAYEMENT') THEN f.code
    ELSE 'PAYEMENT'
END
WHERE service_type IS NULL;

-- Bank transfer DEFAULT = TRANSFERT (pas PAYEMENT)
UPDATE payment_flow f
SET service_type = 'TRANSFERT'
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'BANK_TRANSFER'
  AND f.code = 'DEFAULT';

-- Wallet TRANSFER
UPDATE payment_flow f
SET service_type = 'TRANSFERT'
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'WALLET_TRANSFER'
  AND f.code = 'TRANSFER';

-- Dédupliquer avant UNIQUE (garder le plus ancien)
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY provider_id, service_type ORDER BY created_at ASC, id ASC) AS rn
    FROM payment_flow
    WHERE service_type IS NOT NULL
)
UPDATE payment_flow f
SET service_type = f.code || '_LEGACY_' || substr(replace(f.id::text, '-', ''), 1, 8),
    actif = FALSE
FROM ranked r
WHERE f.id = r.id
  AND r.rn > 1;

ALTER TABLE payment_flow
    ALTER COLUMN service_type SET NOT NULL;

ALTER TABLE payment_flow
    DROP CONSTRAINT IF EXISTS uk_payment_flow_provider_service_type;

ALTER TABLE payment_flow
    ADD CONSTRAINT uk_payment_flow_provider_service_type UNIQUE (provider_id, service_type);

-- Flows manquants pour les service types déjà rattachés aux providers
INSERT INTO payment_flow (id, provider_id, code, nom, bpm_process_key, description, actif, created_at, service_type)
SELECT gen_random_uuid(),
       pst.provider_id,
       pst.service_type_code,
       CASE pst.service_type_code
           WHEN 'CONSULTATION_SOLDE' THEN 'Consultation solde'
           WHEN 'LINK' THEN 'Lien compte'
           WHEN 'UNLINK' THEN 'Déliaison compte'
           WHEN 'TRANSFERT' THEN 'Transfert'
           WHEN 'PAYEMENT' THEN 'Paiement / checkout'
           WHEN 'PAIEMENT_FACTURE' THEN 'Paiement facture'
           ELSE pst.service_type_code
       END,
       CASE pst.service_type_code
           WHEN 'CONSULTATION_SOLDE' THEN 'providerBalanceInquiry'
           WHEN 'LINK' THEN 'providerAccountLink'
           WHEN 'UNLINK' THEN 'providerAccountUnlink'
           WHEN 'TRANSFERT' THEN 'providerWalletTransfer'
           WHEN 'PAYEMENT' THEN 'providerOtpCheckout'
           WHEN 'PAIEMENT_FACTURE' THEN 'providerBillPay'
           ELSE NULL
       END,
       'Flow auto pour ' || pst.service_type_code,
       TRUE,
       NOW(),
       pst.service_type_code
FROM provider_service_type pst
WHERE NOT EXISTS (
    SELECT 1 FROM payment_flow f
    WHERE f.provider_id = pst.provider_id AND f.service_type = pst.service_type_code
);

-- Affiner les bpm keys des flows secondaires
UPDATE payment_flow f
SET bpm_process_key = 'providerBalanceInquiry'
WHERE f.service_type = 'CONSULTATION_SOLDE'
  AND (f.bpm_process_key IS NULL OR f.bpm_process_key = 'providerOtpCheckout');

UPDATE payment_flow f
SET bpm_process_key = 'providerAccountLink'
WHERE f.service_type = 'LINK'
  AND (f.bpm_process_key IS NULL OR f.bpm_process_key IN ('providerOtpCheckout', 'providerRedirectCheckout'));

UPDATE payment_flow f
SET bpm_process_key = 'providerAccountUnlink'
WHERE f.service_type = 'UNLINK'
  AND (f.bpm_process_key IS NULL OR f.bpm_process_key IN ('providerOtpCheckout', 'providerRedirectCheckout'));

UPDATE payment_flow f
SET bpm_process_key = 'providerBankTransfer'
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'BANK_TRANSFER'
  AND f.service_type = 'TRANSFERT';

UPDATE payment_flow f
SET bpm_process_key = 'providerWalletTransfer'
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'WALLET_TRANSFER'
  AND f.service_type = 'TRANSFERT';

UPDATE payment_flow f
SET bpm_process_key = 'providerBillPay'
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'BILL_PAYMENT'
  AND f.service_type IN ('PAIEMENT_FACTURE', 'PAYEMENT')
  AND (f.bpm_process_key IS NULL OR f.bpm_process_key = 'providerOtpCheckout');
