-- Branche le parcours ajout (FSE → INFOCLIENT → récap → save) sur MES_BENEFICIAIRES
-- (au lieu du produit dédié ADD_BENEFICIARY). Conserve la présentation carnet.

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'MES_BENEFICIAIRES',
       'Mes bénéficiaires (orchestration)',
       'Hôte technique du parcours catalogue MES_BENEFICIAIRES',
       TRUE,
       92,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'MES_BENEFICIAIRES');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'MES_BENEFICIAIRES'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'MES_BENEFICIAIRES',
       'Ajout bénéficiaire',
       'TRANSFERT',
       'addBeneficiary',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'MES_BENEFICIAIRES'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.code = 'MES_BENEFICIAIRES'
  );

UPDATE payment_flow f
SET bpm_process_key = 'addBeneficiary',
    nom = 'Ajout bénéficiaire',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'MES_BENEFICIAIRES'
  AND f.service_type = 'TRANSFERT';

UPDATE catalog_de_service_version v
SET workflow_key = 'addBeneficiary',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'journeyCode', 'BENEFICIARIES_BOOK',
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
                    jsonb_build_object(
                            'step', 'CAPTURE_IDENTIFIER', 'kind', 'UI', 'actorRole', 'DESTINATION',
                            'formSource', 'FORM_KEY', 'formKey', 'add-beneficiary-wallet'
                    ),
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
                    'pathAlias', 'mes-beneficiaires',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'optionalAmountAtStart', true,
                    'providersQuery', jsonb_build_object('serviceType', 'TRANSFERT'),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'MES_BENEFICIAIRES',
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
                                    'id', 'beneficiaries',
                                    'displayKind', 'SHORTCUT_SLIDE',
                                    'dataSource', 'TRANSFER_BENEFICIARIES',
                                    'origin', 'LOCAL',
                                    'title', 'Mes bénéficiaires',
                                    'bindsTo', 'beneficiaryId',
                                    'params', jsonb_build_object('limit', 20)
                            ),
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
  AND s.code = 'MES_BENEFICIAIRES';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    name = 'Mes bénéficiaires',
    description = 'Carnet + ajout (recherche titulaire FSE)',
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'MES_BENEFICIAIRES'
  AND v.version = '1.0';

-- ADD_BENEFICIARY reste publié comme alias technique (même BPMN), host réutilise MES_BENEFICIAIRES.
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{gateway,hostFlow,providerCode}',
        '"MES_BENEFICIAIRES"'::jsonb,
        true
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ADD_BENEFICIARY';

UPDATE catalog_de_service s
SET description = 'Alias de MES_BENEFICIAIRES (ajout bénéficiaire)',
    updated_at = NOW()
WHERE s.code = 'ADD_BENEFICIARY';
