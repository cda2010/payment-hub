-- KYC / statut client exposés au gateway (EVALUATE_RULES).

ALTER TABLE identity_party
    ADD COLUMN IF NOT EXISTS kyc_level VARCHAR(32) NOT NULL DEFAULT 'STANDARD';

ALTER TABLE identity_party
    ADD COLUMN IF NOT EXISTS customer_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';

UPDATE identity_party SET kyc_level = 'STANDARD' WHERE kyc_level IS NULL OR kyc_level = '';
UPDATE identity_party SET customer_status = CASE WHEN active THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE customer_status IS NULL OR customer_status = '';
