-- Aligne compte_fse sur le schéma Omnicanal public.compte
-- (FKs bigint Omnicanal → codes / UUID payment-2)

ALTER TABLE compte_fse
    ADD COLUMN IF NOT EXISTS omni_id            BIGINT,
    ADD COLUMN IF NOT EXISTS code_banque        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS code_agence        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS cle                VARCHAR(5),
    ADD COLUMN IF NOT EXISTS extype             VARCHAR(20),
    ADD COLUMN IF NOT EXISTS devise             VARCHAR(10),
    ADD COLUMN IF NOT EXISTS fse_titulaire      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS libelle_compte     VARCHAR(200),
    ADD COLUMN IF NOT EXISTS code_uo_titulaire  VARCHAR(20),
    ADD COLUMN IF NOT EXISTS mirroir_id         UUID,
    ADD COLUMN IF NOT EXISTS agence_code        VARCHAR(20);

-- Backfill depuis les colonnes déjà présentes
UPDATE compte_fse
SET code_banque    = COALESCE(NULLIF(TRIM(code_banque), ''), '00000'),
    code_agence    = COALESCE(code_agence, ''),
    extype         = COALESCE(NULLIF(TRIM(extype), ''), 'CompteFse'),
    fse_titulaire  = COALESCE(NULLIF(TRIM(fse_titulaire), ''), fse_teneur),
    libelle_compte = COALESCE(libelle_compte, ''),
    agence_code    = COALESCE(
        NULLIF(TRIM(agence_code), ''),
        COALESCE(NULLIF(TRIM(code_agence), ''), '01001')
    );

ALTER TABLE compte_fse
    ALTER COLUMN code_banque SET DEFAULT '00000',
    ALTER COLUMN code_agence SET DEFAULT '',
    ALTER COLUMN extype SET DEFAULT 'CompteFse',
    ALTER COLUMN libelle_compte SET DEFAULT '',
    ALTER COLUMN agence_code SET DEFAULT '01001';

ALTER TABLE compte_fse
    ALTER COLUMN code_banque SET NOT NULL,
    ALTER COLUMN code_agence SET NOT NULL,
    ALTER COLUMN extype SET NOT NULL,
    ALTER COLUMN fse_titulaire SET NOT NULL,
    ALTER COLUMN libelle_compte SET NOT NULL,
    ALTER COLUMN agence_code SET NOT NULL;

-- Longueurs alignées Omnicanal
ALTER TABLE compte_fse
    ALTER COLUMN numero_compte TYPE VARCHAR(20);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_compte_fse_mirroir'
    ) THEN
        ALTER TABLE compte_fse
            ADD CONSTRAINT fk_compte_fse_mirroir
            FOREIGN KEY (mirroir_id) REFERENCES compte_fse (id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_compte_fse_omni_id
    ON compte_fse (omni_id)
    WHERE omni_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_compte_fse_titulaire
    ON compte_fse (fse_titulaire, type_compte_code);

CREATE INDEX IF NOT EXISTS idx_compte_fse_agence
    ON compte_fse (agence_code);
