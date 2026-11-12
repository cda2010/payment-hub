-- Code FSE (fournisseur_service) choisi sur le facturier, sans ligne payment_provider.

ALTER TABLE biller
    ADD COLUMN IF NOT EXISTS provider_code varchar(40);

ALTER TABLE biller_product
    ADD COLUMN IF NOT EXISTS provider_code varchar(40);
