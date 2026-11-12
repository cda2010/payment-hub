-- Tous les parcours démo passent en BPMN (bpm_process_key) ; plus de payment_flow_step.

UPDATE payment_flow f
SET bpm_process_key = 'providerOtpCheckout'
FROM payment_provider p
WHERE f.provider_id = p.id AND p.code = 'CORIS_MONEY' AND f.code = 'DEFAULT';

UPDATE payment_flow f
SET bpm_process_key = 'providerOrangeOtp'
FROM payment_provider p
WHERE f.provider_id = p.id AND p.code = 'ORANGE_MONEY' AND f.code = 'DEFAULT';

UPDATE payment_flow f
SET bpm_process_key = 'providerRedirectCheckout'
FROM payment_provider p
WHERE f.provider_id = p.id AND p.code IN ('WAVE', 'CARD_PAYMENT') AND f.code = 'DEFAULT';

UPDATE payment_flow f
SET bpm_process_key = 'providerBankTransfer'
FROM payment_provider p
WHERE f.provider_id = p.id AND p.code = 'BANK_TRANSFER' AND f.code = 'DEFAULT';

UPDATE payment_flow f
SET bpm_process_key = 'providerWalletTransfer'
FROM payment_provider p
WHERE f.provider_id = p.id AND p.code = 'WALLET_TRANSFER' AND f.code = 'TRANSFER';

UPDATE payment_flow f
SET bpm_process_key = 'providerBillPay'
FROM payment_provider p
WHERE f.provider_id = p.id AND p.code = 'BILL_PAYMENT' AND f.code = 'BILL_PAY';

DELETE FROM payment_flow_step
WHERE flow_id IN (
    SELECT id FROM payment_flow WHERE bpm_process_key IS NOT NULL AND bpm_process_key <> ''
);
