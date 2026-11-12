-- Phase A FSE : identité fournisseur (nullable — ne casse pas les providers existants)

ALTER TABLE payment_provider
    ADD COLUMN IF NOT EXISTS code_fse VARCHAR(20),
    ADD COLUMN IF NOT EXISTS code_externe VARCHAR(50),
    ADD COLUMN IF NOT EXISTS group_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS country_code VARCHAR(5),
    ADD COLUMN IF NOT EXISTS participant_kind VARCHAR(20),
    ADD COLUMN IF NOT EXISTS fse_statut VARCHAR(20);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_provider_code_fse
    ON payment_provider (code_fse)
    WHERE code_fse IS NOT NULL;

-- Lien optionnel participant transfert → fournisseur gateway
ALTER TABLE transfer_participant
    ADD COLUMN IF NOT EXISTS provider_id UUID REFERENCES payment_provider(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_transfer_participant_provider
    ON transfer_participant (provider_id);

-- Backfill CORIS MONEY (code_fse Omnicanal 0011)
UPDATE payment_provider
SET code_fse = '0011',
    code_externe = COALESCE(code_externe, '0011'),
    group_code = COALESCE(group_code, 'CORIS'),
    country_code = COALESCE(country_code, 'BF'),
    participant_kind = COALESCE(participant_kind, 'WALLET'),
    fse_statut = COALESCE(fse_statut, 'NONBANQUE')
WHERE code = 'CORIS_MONEY'
  AND code_fse IS NULL;

-- Lien automatique participant.code = provider.code
UPDATE transfer_participant tp
SET provider_id = pp.id
FROM payment_provider pp
WHERE tp.provider_id IS NULL
  AND upper(tp.code) = upper(pp.code);
