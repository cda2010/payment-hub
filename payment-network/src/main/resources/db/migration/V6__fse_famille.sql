-- Famille d'un FSE : mobile money ou banque (banque et méso finance dans la même liste).

ALTER TABLE unite_organisation ADD COLUMN IF NOT EXISTS famille_fse VARCHAR(16);

ALTER TABLE unite_organisation DROP CONSTRAINT IF EXISTS ck_unite_famille_fse;
ALTER TABLE unite_organisation ADD CONSTRAINT ck_unite_famille_fse
    CHECK (famille_fse IS NULL OR famille_fse IN ('MOBILE_MONEY', 'BANQUE'));

UPDATE unite_organisation
SET famille_fse = 'MOBILE_MONEY'
WHERE code IN ('FSE-UEMOA', 'FSE-CEMAC')
  AND (famille_fse IS NULL OR famille_fse = '');

INSERT INTO unite_organisation (
    id, code, libelle, type, zone, parent_id, statut, niveau, chemin,
    version, created_by, updated_by, code_fse, famille_fse
)
SELECT 'b1000000-0000-4000-8000-000000000011',
       'FSE-CORIS-BANK',
       'Coris Bank',
       'FSE',
       p.zone,
       p.id,
       'ACTIF',
       p.niveau + 1,
       p.chemin || 'b1000000-0000-4000-8000-000000000011/',
       0,
       'seed',
       'seed',
       '0101',
       'BANQUE'
FROM unite_organisation p
WHERE p.code = 'BF'
  AND NOT EXISTS (SELECT 1 FROM unite_organisation WHERE code = 'FSE-CORIS-BANK');

INSERT INTO unite_organisation (
    id, code, libelle, type, zone, parent_id, statut, niveau, chemin,
    version, created_by, updated_by, code_fse, famille_fse
)
SELECT 'b1000000-0000-4000-8000-000000000012',
       'FSE-CORIS-MESO',
       'Coris Meso Finance',
       'FSE',
       p.zone,
       p.id,
       'ACTIF',
       p.niveau + 1,
       p.chemin || 'b1000000-0000-4000-8000-000000000012/',
       0,
       'seed',
       'seed',
       '0102',
       'BANQUE'
FROM unite_organisation p
WHERE p.code = 'BF'
  AND NOT EXISTS (SELECT 1 FROM unite_organisation WHERE code = 'FSE-CORIS-MESO');
