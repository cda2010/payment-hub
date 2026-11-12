-- Type de service générique sélectionnable + repli catalogue (plus de mapping Java produit→type).

INSERT INTO service_type (code, libelle, category, description, actif, ordre_affichage) VALUES
    ('DEFAUT', 'Par défaut', 'OTHER',
     'Type générique pour un produit catalogue sans famille métier plus précise (LINK, TRANSFERT, facture…)',
     TRUE, 5)
ON CONFLICT (code) DO UPDATE
SET libelle = EXCLUDED.libelle,
    category = EXCLUDED.category,
    description = EXCLUDED.description,
    actif = TRUE;

-- Versions catalogue sans serviceType → DEFAUT (admin pourra ensuite choisir un type précis).
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object('serviceType', 'DEFAUT'),
    updated_at = NOW()
WHERE v.configuration IS NULL
   OR v.configuration ->> 'serviceType' IS NULL
   OR btrim(v.configuration ->> 'serviceType') = '';
