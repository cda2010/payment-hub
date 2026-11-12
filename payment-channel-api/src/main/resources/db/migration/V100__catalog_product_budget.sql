-- Produit catalogue BUDGET (enveloppes mensuelles — plugin mobile).

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'BUDGET',
       'Budget',
       'Suivi des dépenses par enveloppe mensuelle',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'SAVINGS'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'BUDGET');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       NULL,
       jsonb_build_object(
               'serviceType', 'PAYEMENT',
               'journeyCode', 'BUDGET_TRACK',
               'supportedChannels', jsonb_build_array('MOBILE'),
               'defaultChannel', 'MOBILE',
               'presentation', jsonb_build_object(
                       'version', 1,
                       'channel', 'MOBILE',
                       'sections', jsonb_build_array()
               )
       ),
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'BUDGET'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BUDGET'
  AND v.version = '1.0';
