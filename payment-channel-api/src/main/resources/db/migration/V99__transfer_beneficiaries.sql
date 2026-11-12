-- Bénéficiaires de transfert (carnets contacts) + section présentation ACCOUNT_TRANSFER
-- + produit catalogue MES_BENEFICIAIRES (gestion / plugin mobile).

CREATE TABLE IF NOT EXISTS transfer_beneficiary (
    id                    UUID PRIMARY KEY,
    label                 VARCHAR(150) NOT NULL,
    phone                 VARCHAR(30),
    account_number        VARCHAR(64),
    destination_provider  VARCHAR(50) NOT NULL,
    icon                  VARCHAR(60),
    owner_key             VARCHAR(80),
    actif                 BOOLEAN NOT NULL DEFAULT TRUE,
    ordre_affichage       INT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transfer_beneficiary_actif
    ON transfer_beneficiary (actif, ordre_affichage);

-- Seed démo (idempotent)
INSERT INTO transfer_beneficiary (
    id, label, phone, account_number, destination_provider, icon, owner_key, actif, ordre_affichage, created_at, updated_at
)
SELECT gen_random_uuid(), v.label, v.phone, v.account_number, v.destination_provider, v.icon, 'demo', TRUE, v.ord, NOW(), NOW()
FROM (VALUES
          ('Awa Ouédraogo', '70000001', NULL, 'ORANGE_MONEY', 'person-outline', 10),
          ('Issa Kaboré', '70000002', NULL, 'MOOV_MONEY', 'person-outline', 20),
          ('Fatim Traoré', '70000003', NULL, 'CORIS_MONEY', 'person-outline', 30),
          ('Compte pro BF', NULL, 'BF00CORIS000123', 'CORIS_BANK', 'business-outline', 40)
     ) AS v(label, phone, account_number, destination_provider, icon, ord)
WHERE NOT EXISTS (SELECT 1 FROM transfer_beneficiary LIMIT 1);

-- ── Produit catalogue MES_BENEFICIAIRES (gestion carnet) ───────────────────
INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'MES_BENEFICIAIRES',
       'Mes bénéficiaires',
       'Carnet de bénéficiaires pour les transferts',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'TRANSFER'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'MES_BENEFICIAIRES');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       NULL,
       jsonb_build_object(
               'serviceType', 'TRANSFERT',
               'journeyCode', 'BENEFICIARIES_BOOK',
               'supportedChannels', jsonb_build_array('MOBILE'),
               'defaultChannel', 'MOBILE',
               'presentation', jsonb_build_object(
                       'version', 1,
                       'channel', 'MOBILE',
                       'sections', jsonb_build_array(
                               jsonb_build_object(
                                       'id', 'beneficiaries',
                                       'displayKind', 'SHORTCUT_SLIDE',
                                       'dataSource', 'TRANSFER_BENEFICIARIES',
                                       'origin', 'LOCAL',
                                       'title', 'Mes bénéficiaires',
                                       'bindsTo', 'beneficiaryId',
                                       'params', jsonb_build_object('limit', 20)
                               )
                       )
               )
       ),
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'MES_BENEFICIAIRES'
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
  AND s.code = 'MES_BENEFICIAIRES'
  AND v.version = '1.0';

-- ── ACCOUNT_TRANSFER : section bénéficiaires (avant destinations) ───────────
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{presentation,sections}',
        (
            jsonb_build_array(
                    jsonb_build_object(
                            'id', 'beneficiaries',
                            'displayKind', 'SHORTCUT_SLIDE',
                            'dataSource', 'TRANSFER_BENEFICIARIES',
                            'origin', 'LOCAL',
                            'title', 'Mes bénéficiaires',
                            'bindsTo', 'beneficiaryId',
                            'params', jsonb_build_object('limit', 12)
                    )
            )
            || COALESCE(v.configuration -> 'presentation' -> 'sections', '[]'::jsonb)
        ),
        true
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'presentation' -> 'sections', '[]'::jsonb)) sec
        WHERE sec ->> 'id' = 'beneficiaries'
           OR sec ->> 'bindsTo' = 'beneficiaryId'
    );
