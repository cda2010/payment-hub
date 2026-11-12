-- Clé process Flowable enregistrée sur le parcours (comme le code flow côté moteur)
ALTER TABLE payment_flow
    ADD COLUMN IF NOT EXISTS bpm_process_key VARCHAR(100);

COMMENT ON COLUMN payment_flow.bpm_process_key IS
    'Clé de définition Flowable (process id). Null = orchestration PaymentEngine classique.';

-- Coris-like OTP : parcours avec étape GENERATE_OTP → providerOtpCheckout
UPDATE payment_flow f
SET bpm_process_key = 'providerOtpCheckout'
WHERE f.bpm_process_key IS NULL
  AND f.code = 'DEFAULT'
  AND EXISTS (
      SELECT 1 FROM payment_flow_step s
      WHERE s.flow_id = f.id AND s.code = 'GENERATE_OTP'
  );

CREATE INDEX IF NOT EXISTS idx_payment_flow_bpm_key ON payment_flow (bpm_process_key);
