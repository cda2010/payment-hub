-- Schémas UI configurables par produit facturier

ALTER TABLE biller_product
    ADD COLUMN IF NOT EXISTS lookup_fields_json TEXT,
    ADD COLUMN IF NOT EXISTS confirm_fields_json TEXT,
    ADD COLUMN IF NOT EXISTS amount_fields_json TEXT;
