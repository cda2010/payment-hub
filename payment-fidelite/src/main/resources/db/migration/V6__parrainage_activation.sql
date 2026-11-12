-- Le code associe. La prime part après le KYC et une première transaction éligible.
-- Les montants de points restent points_parrain et points_filleul.

ALTER TABLE programme ADD COLUMN referral_kyc_required BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE programme ADD COLUMN referral_min_amount NUMERIC(19, 2) NOT NULL DEFAULT 5000;
ALTER TABLE programme ADD COLUMN referral_currency VARCHAR(3) NOT NULL DEFAULT 'XOF';
ALTER TABLE programme ADD COLUMN referral_qualification_days INT NOT NULL DEFAULT 30;
ALTER TABLE programme ADD COLUMN referral_max_per_month INT NOT NULL DEFAULT 20;
ALTER TABLE programme ADD COLUMN referral_products VARCHAR(400);

ALTER TABLE parrainage ADD COLUMN kyc_approved BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE parrainage ADD COLUMN eligible_event_id UUID;
ALTER TABLE parrainage ADD COLUMN eligible_at TIMESTAMPTZ;
ALTER TABLE parrainage ADD COLUMN qualified_at TIMESTAMPTZ;
ALTER TABLE parrainage ADD COLUMN rejection_reason VARCHAR(40);

CREATE UNIQUE INDEX parrainage_filleul_uk
    ON parrainage (programme_id, filleul_party_id)
    WHERE filleul_party_id IS NOT NULL;
