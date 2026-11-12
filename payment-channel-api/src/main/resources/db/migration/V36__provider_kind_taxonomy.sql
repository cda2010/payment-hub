-- Provider kind: WALLET | BANQUE | FACTURIER | REMITTANCE | RESEAU

UPDATE payment_provider
SET participant_kind = 'BANQUE'
WHERE participant_kind = 'BANK';

UPDATE payment_provider
SET participant_kind = 'FACTURIER'
WHERE participant_kind = 'BILLER';

UPDATE payment_provider
SET participant_kind = 'RESEAU'
WHERE participant_kind = 'OTHER';

UPDATE payment_provider
SET participant_kind = 'FACTURIER'
WHERE code = 'BILL_PAYMENT';

UPDATE payment_provider
SET participant_kind = 'BANQUE'
WHERE code IN ('CORIS_BANK', 'BANK_TRANSFER')
  AND (participant_kind IS NULL OR participant_kind NOT IN ('WALLET', 'BANQUE', 'FACTURIER', 'REMITTANCE', 'RESEAU'));

UPDATE payment_provider
SET participant_kind = 'WALLET'
WHERE code IN ('CORIS_MONEY', 'ORANGE_MONEY', 'WAVE')
  AND (participant_kind IS NULL OR participant_kind NOT IN ('WALLET', 'BANQUE', 'FACTURIER', 'REMITTANCE', 'RESEAU'));

UPDATE payment_provider
SET participant_kind = 'RESEAU'
WHERE code = 'WALLET_TRANSFER'
  AND (participant_kind IS NULL OR participant_kind NOT IN ('WALLET', 'BANQUE', 'FACTURIER', 'REMITTANCE', 'RESEAU'));
