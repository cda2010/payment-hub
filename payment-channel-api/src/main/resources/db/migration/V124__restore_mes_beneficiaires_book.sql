-- Restaure MES_BENEFICIAIRES en carnet présentation-only (V99)
-- et resépare ADD_BENEFICIARY comme parcours d'ajout (V121).

-- ── 1) MES_BENEFICIAIRES → carnet (plus de BPMN / steps) ───────────────────
UPDATE catalog_de_service_version v
SET workflow_key = NULL,
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
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
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'MES_BENEFICIAIRES';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    name = 'Mes bénéficiaires',
    description = 'Carnet de bénéficiaires pour les transferts',
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'MES_BENEFICIAIRES'
  AND v.version = '1.0';

-- Hôte technique MES_BENEFICIAIRES (créé en V122) : plus utilisé pour un journey.
UPDATE payment_flow f
SET actif = FALSE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'MES_BENEFICIAIRES'
  AND f.code = 'MES_BENEFICIAIRES';

-- ── 2) ADD_BENEFICIARY → parcours dédié (reprise V121) ─────────────────────
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
                    'pathAlias', 'add-beneficiary',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'optionalAmountAtStart', true,
                    'providersQuery', jsonb_build_object('serviceType', 'TRANSFERT'),
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
