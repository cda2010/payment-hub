-- ADD_BENEFICIARY : un seul écran UI (réseau + identifiant) avant INFOCLIENT.

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
                                    'displayKind', 'SHORTCUT_SLIDE',
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
    description = 'Réseau + identifiant → INFOCLIENT → enregistrement carnet',
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ADD_BENEFICIARY'
  AND v.version = '1.0';
