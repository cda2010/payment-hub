-- Coris Bank : code agence + N° de compte (INFOCLIENT + carnet).

ALTER TABLE transfer_beneficiary
    ADD COLUMN IF NOT EXISTS agency_code VARCHAR(20);

-- Schéma FSE carnet / destinations pour participants banque.
UPDATE transfer_participant
SET beneficiary_fields_json = '[{"name":"agence","label":"Code agence","type":"text","required":true,"placeholder":"ex. 01001"},{"name":"accountNumber","label":"N° de compte","type":"text","required":true,"placeholder":"N° de compte"}]',
    updated_at = NOW()
WHERE actif = TRUE
  AND (
        participant_kind = 'BANK'
     OR upper(code) LIKE '%BANK%'
  );

-- Récap ADD_BENEFICIARY : afficher code agence.
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{recap,fields}',
        '[
          {"key":"destinationProvider","label":"Réseau"},
          {"key":"beneficiaryName","label":"Titulaire"},
          {"key":"phoneNumber","label":"Téléphone"},
          {"key":"agence","label":"Code agence"},
          {"key":"numeroCompte","label":"N° de compte"}
        ]'::jsonb,
        true
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ADD_BENEFICIARY';
