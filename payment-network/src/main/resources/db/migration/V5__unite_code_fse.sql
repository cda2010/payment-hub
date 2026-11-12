-- Code FSE Omnicanal sur l'unité d'organisation de type FSE (ex. 0011 Coris Money).
-- Obligatoire à la création d'une FSE ; null pour les autres types.

ALTER TABLE unite_organisation
    ADD COLUMN IF NOT EXISTS code_fse VARCHAR(20);

COMMENT ON COLUMN unite_organisation.code_fse IS
    'Code FSE Omnicanal (providers / interfaces). Renseigné uniquement si type = FSE.';

-- Une FSE → un code FSE unique dans le réseau.
CREATE UNIQUE INDEX IF NOT EXISTS uq_unite_code_fse
    ON unite_organisation (code_fse)
    WHERE code_fse IS NOT NULL;

-- Seed démo : Coris Money (0011) pour UEMOA et CEMAC.
UPDATE unite_organisation SET code_fse = '0011' WHERE code = 'FSE-UEMOA' AND type = 'FSE';
UPDATE unite_organisation SET code_fse = '0012' WHERE code = 'FSE-CEMAC' AND type = 'FSE';
