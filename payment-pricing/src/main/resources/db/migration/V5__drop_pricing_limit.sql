-- Les plafonds / cumuls réglementaires sont portés par payment-rules.
-- pricing ne conserve que frais, taux et policies produit/canal.

ALTER TABLE pricing_policy DROP COLUMN IF EXISTS limit_code;

DROP TABLE IF EXISTS pricing_limit;
