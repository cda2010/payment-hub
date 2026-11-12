-- Rattacher facturiers / produits à un PaymentProvider (type FACTURIER)

ALTER TABLE biller
    ADD COLUMN IF NOT EXISTS provider_id UUID;

ALTER TABLE biller_product
    ADD COLUMN IF NOT EXISTS provider_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_biller_provider'
    ) THEN
        ALTER TABLE biller
            ADD CONSTRAINT fk_biller_provider
            FOREIGN KEY (provider_id) REFERENCES payment_provider (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_biller_product_provider'
    ) THEN
        ALTER TABLE biller_product
            ADD CONSTRAINT fk_biller_product_provider
            FOREIGN KEY (provider_id) REFERENCES payment_provider (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_biller_provider ON biller (provider_id);
CREATE INDEX IF NOT EXISTS idx_biller_product_provider ON biller_product (provider_id);

-- Rattacher les facturiers existants au rail BILL_PAYMENT (kind FACTURIER)
UPDATE biller b
SET provider_id = p.id
FROM payment_provider p
WHERE p.code = 'BILL_PAYMENT'
  AND b.provider_id IS NULL;

UPDATE biller_product bp
SET provider_id = b.provider_id
FROM biller b
WHERE bp.biller_id = b.id
  AND bp.provider_id IS NULL
  AND b.provider_id IS NOT NULL;
