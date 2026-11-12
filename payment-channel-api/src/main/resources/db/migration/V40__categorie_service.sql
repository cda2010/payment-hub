-- Catégorie de regroupement des CatalogDeService
CREATE TABLE IF NOT EXISTS categorie_service (
    id               UUID         PRIMARY KEY,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    ordre_affichage  INTEGER,
    actif            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_categorie_service_actif ON categorie_service (actif);

-- Seed des catégories déjà utilisées dans catalog_de_service.category
INSERT INTO categorie_service (id, code, name, description, ordre_affichage, actif, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'TRANSFER', 'Transferts', 'Transferts de compte / wallet / banque', 10, TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'BILLING', 'Facturation', 'Paiements facturiers', 20, TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'REMITTANCE', 'Remittance', 'Envois d''argent / remittance', 30, TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'CASH', 'Cash', 'Cash-in / cash-out', 40, TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SAVINGS', 'Épargne / tontine', 'Produits d''épargne collective', 50, TRUE, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Colonnes distinctes pour toute catégorie encore absente du seed
INSERT INTO categorie_service (id, code, name, description, ordre_affichage, actif, created_at, updated_at)
SELECT
    gen_random_uuid(),
    UPPER(TRIM(s.category)),
    UPPER(TRIM(s.category)),
    NULL,
    100,
    TRUE,
    NOW(),
    NOW()
FROM catalog_de_service s
WHERE s.category IS NOT NULL
  AND TRIM(s.category) <> ''
  AND NOT EXISTS (
      SELECT 1 FROM categorie_service c WHERE c.code = UPPER(TRIM(s.category))
  )
GROUP BY UPPER(TRIM(s.category));

ALTER TABLE catalog_de_service
    ADD COLUMN IF NOT EXISTS categorie_id UUID;

UPDATE catalog_de_service s
SET categorie_id = c.id
FROM categorie_service c
WHERE c.code = UPPER(TRIM(s.category))
  AND s.categorie_id IS NULL;

-- Fallback : rattacher les orphelins à TRANSFER
UPDATE catalog_de_service s
SET categorie_id = c.id
FROM categorie_service c
WHERE c.code = 'TRANSFER'
  AND s.categorie_id IS NULL;

ALTER TABLE catalog_de_service
    ALTER COLUMN categorie_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_catalog_de_service_categorie'
    ) THEN
        ALTER TABLE catalog_de_service
            ADD CONSTRAINT fk_catalog_de_service_categorie
            FOREIGN KEY (categorie_id) REFERENCES categorie_service(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_catalog_de_service_categorie ON catalog_de_service (categorie_id);

ALTER TABLE catalog_de_service
    DROP COLUMN IF EXISTS category;
