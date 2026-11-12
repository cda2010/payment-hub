-- Catégorie métier du moyen de paiement (Carte / Compte bancaire / Mobile Money)
ALTER TABLE payment_provider
    ADD COLUMN IF NOT EXISTS payment_method_type VARCHAR(30) NOT NULL DEFAULT 'MOBILE_MONEY';

UPDATE payment_provider
SET payment_method_type = 'MOBILE_MONEY'
WHERE payment_method_type IS NULL
   OR payment_method_type = '';
