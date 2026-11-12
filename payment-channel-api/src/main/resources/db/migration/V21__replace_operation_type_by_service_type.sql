-- Remplace operation_type par service_type comme seule intention produit
INSERT INTO service_type (code, libelle, category, description, actif, ordre_affichage) VALUES
    ('PAIEMENT_FACTURE', 'Paiement de facture', 'BILLING', 'Paiement facturier (ex-BILL_PAYMENT / Omnicanal PAIEMENT_FACTURE)', TRUE, 55)
ON CONFLICT (code) DO NOTHING;

-- Migrer les anciennes valeurs operation_type → service_type
UPDATE payment_transaction
SET service_type = CASE
    WHEN service_type IS NOT NULL AND service_type <> '' THEN service_type
    WHEN operation_type IN ('TRANSFER', 'TRANSFERT') THEN 'TRANSFERT'
    WHEN operation_type = 'BILL_PAYMENT' THEN 'PAIEMENT_FACTURE'
    ELSE 'PAYEMENT'
END;

ALTER TABLE payment_transaction
    ALTER COLUMN service_type SET NOT NULL;

-- Providers facture : rattacher PAIEMENT_FACTURE
INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAIEMENT_FACTURE'
FROM payment_provider p
WHERE p.code = 'BILL_PAYMENT'
ON CONFLICT DO NOTHING;

-- Retirer l’ancien champ
ALTER TABLE payment_transaction DROP COLUMN IF EXISTS operation_type;
