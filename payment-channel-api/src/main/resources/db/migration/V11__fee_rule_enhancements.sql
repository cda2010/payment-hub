-- Grilles de frais enrichies : fixe + %, min/max, porteur des frais

ALTER TABLE fee_rule
    ADD COLUMN IF NOT EXISTS fee_bearer VARCHAR(20) NOT NULL DEFAULT 'SENDER';

ALTER TABLE fee_rule_line
    ADD COLUMN IF NOT EXISTS fixed_amount NUMERIC(18, 2),
    ADD COLUMN IF NOT EXISTS percent_value NUMERIC(18, 4),
    ADD COLUMN IF NOT EXISTS min_fee NUMERIC(18, 2),
    ADD COLUMN IF NOT EXISTS max_fee NUMERIC(18, 2);

-- Rétrocompat : peupler fixed_amount / percent_value depuis fee_type + value
UPDATE fee_rule_line
SET fixed_amount = value
WHERE fee_type = 'FIXED' AND fixed_amount IS NULL AND percent_value IS NULL;

UPDATE fee_rule_line
SET percent_value = value
WHERE fee_type = 'PERCENT' AND fixed_amount IS NULL AND percent_value IS NULL;
