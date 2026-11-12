-- ADD_BENEFICIARY : choix FSE → saisie contextuelle → INFOCLIENT → récap → save carnet

INSERT INTO categorie_service (id, code, name, description, ordre_affichage, actif, created_at, updated_at)
SELECT gen_random_uuid(), 'TRANSFER', 'Transfert', 'Transferts et carnet', 20, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM categorie_service WHERE code = 'TRANSFER');

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'ADD_BENEFICIARY',
       'Ajout de bénéficiaire',
       'Recherche titulaire FSE (wallet téléphone / banque compte) puis enregistrement carnet',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'TRANSFER'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'ADD_BENEFICIARY');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       'addBeneficiary',
       '{}'::jsonb,
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'ADD_BENEFICIARY'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'ADD_BENEFICIARY',
       'Ajout bénéficiaire (orchestration)',
       'Hôte technique du parcours catalogue ADD_BENEFICIARY',
       TRUE,
       91,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'ADD_BENEFICIARY');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'ADD_BENEFICIARY'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'ADD_BENEFICIARY',
       'Ajout bénéficiaire',
       'TRANSFERT',
       'addBeneficiary',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'ADD_BENEFICIARY'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT' AND f.code = 'ADD_BENEFICIARY'
  );

UPDATE payment_flow f
SET bpm_process_key = 'addBeneficiary',
    nom = 'Ajout bénéficiaire',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'ADD_BENEFICIARY'
  AND f.service_type = 'TRANSFERT';

UPDATE catalog_de_service_version v
SET workflow_key = 'addBeneficiary',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'journeyCode', 'ADD_BENEFICIARY',
            'terminalOnConfirm', false,
            'supportedChannels', jsonb_build_array('MOBILE'),
            'defaultChannel', 'MOBILE',
            'steps', jsonb_build_array(
                    'SELECT_PROVIDER',
                    'CAPTURE_IDENTIFIER',
                    'INFO_CLIENT',
                    'CONFIRM',
                    'SAVE_TRANSFER_BENEFICIARY',
                    'NOTIFY'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'DESTINATION',
                            'label', 'Réseau du bénéficiaire',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'CORIS_MONEY', 'default', true),
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'WAVE', 'default', false),
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'CORIS_BANK', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object(
                            'step', 'SELECT_PROVIDER', 'kind', 'UI', 'actorRole', 'DESTINATION',
                            'formSource', 'FORM_KEY', 'formKey', 'add-beneficiary-provider'
                    ),
                    -- Wallet (défaut) : numéro de téléphone
                    jsonb_build_object(
                            'step', 'CAPTURE_IDENTIFIER', 'kind', 'UI', 'actorRole', 'DESTINATION',
                            'formSource', 'FORM_KEY', 'formKey', 'add-beneficiary-wallet'
                    ),
                    -- Banque : numéro de compte (override FSE)
                    jsonb_build_object(
                            'step', 'CAPTURE_IDENTIFIER', 'kind', 'UI', 'actorRole', 'DESTINATION',
                            'providerCode', 'CORIS_BANK',
                            'formSource', 'FORM_KEY', 'formKey', 'add-beneficiary-bank'
                    ),
                    jsonb_build_object(
                            'step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'DESTINATION',
                            'formSource', 'RECAP', 'formKey', 'product-recap'
                    )
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'add-beneficiary',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'optionalAmountAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'serviceType', 'TRANSFERT'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'ADD_BENEFICIARY',
                            'participantKind', 'RESEAU',
                            'serviceType', 'TRANSFERT'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'phoneNumber', 'accountNumber', 'destinationProvider', 'msisdn'
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Confirmer le bénéficiaire',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'destinationProvider', 'label', 'Réseau'),
                            jsonb_build_object('key', 'beneficiaryName', 'label', 'Titulaire'),
                            jsonb_build_object('key', 'phoneNumber', 'label', 'Téléphone'),
                            jsonb_build_object('key', 'accountNumber', 'label', 'N° de compte')
                    )
            ),
            'softRules', jsonb_build_array(),
            'presentation', jsonb_build_object(
                    'version', 1,
                    'channel', 'MOBILE',
                    'sections', jsonb_build_array(
                            jsonb_build_object(
                                    'id', 'networks',
                                    'displayKind', 'ENTRY_GRID',
                                    'dataSource', 'TRANSFER_PARTICIPANTS',
                                    'origin', 'LOCAL',
                                    'title', 'Réseau',
                                    'bindsTo', 'destinationProvider',
                                    'params', jsonb_build_object('canReceive', true)
                            )
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ADD_BENEFICIARY';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    name = 'Ajout de bénéficiaire',
    description = 'Recherche titulaire FSE puis enregistrement carnet',
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ADD_BENEFICIARY'
  AND v.version = '1.0';
