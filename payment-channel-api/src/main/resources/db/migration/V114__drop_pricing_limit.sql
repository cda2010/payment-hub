-- Alignement avec payment-pricing V5 : plafonds délégués à payment-rules.
-- Tables pricing historiques éventuellement restées sur la base gateway (V112).

ALTER TABLE IF EXISTS pricing_policy DROP COLUMN IF EXISTS limit_code;

DROP TABLE IF EXISTS pricing_limit;
