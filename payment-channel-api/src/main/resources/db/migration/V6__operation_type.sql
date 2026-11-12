-- Type d'opération produit (checkout / transfert / facture)
ALTER TABLE payment_transaction
    ADD COLUMN IF NOT EXISTS operation_type VARCHAR(30) NOT NULL DEFAULT 'CHECKOUT';

UPDATE payment_transaction
SET operation_type = 'CHECKOUT'
WHERE operation_type IS NULL OR operation_type = '';
