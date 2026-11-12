-- REMITTANCE_RECEIVE : retrait / crédit après envoi AVAILABLE_FOR_PAYOUT

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'REMITTANCE_RECEIVE',
       'Remittance receive / payout',
       'Retrait cash ou crédit compte suite à un envoi remittance',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'REMITTANCE'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'REMITTANCE_RECEIVE');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       'remittanceReceive',
       '{}'::jsonb,
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'REMITTANCE_RECEIVE'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'REMITTANCE_RECEIVE',
       'Remittance receive (orchestration)',
       'Hôte technique du parcours catalogue REMITTANCE_RECEIVE',
       TRUE,
       96,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'REMITTANCE_RECEIVE');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'REMITTANCE_RECEIVE'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'REMITTANCE_RECEIVE',
       'Réception remittance',
       'TRANSFERT',
       'remittanceReceive',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'REMITTANCE_RECEIVE'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.code = 'REMITTANCE_RECEIVE'
  );

UPDATE payment_flow f
SET bpm_process_key = 'remittanceReceive',
    nom = 'Réception remittance',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'REMITTANCE_RECEIVE';

UPDATE catalog_de_service_version v
SET workflow_key = 'remittanceReceive',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'terminalOnConfirm', false,
            'steps', jsonb_build_array(
                    'CAPTURE_REFERENCE',
                    'LOOKUP_REMITTANCE',
                    'CAPTURE_BENEFICIARY_ID',
                    'CAPTURE_ACCOUNT_CONFIRM',
                    'VALIDATE_PAYOUT',
                    'CONFIRM_PAYOUT',
                    'EXECUTE_PAYOUT',
                    'COMPLETE_PAYOUT',
                    'NOTIFY'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'PROVIDER',
                            'label', 'Opérateur remittance',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'PROVIDER', 'providerCode', 'RIA', 'default', true),
                    jsonb_build_object('role', 'PROVIDER', 'providerCode', 'WESTERN_UNION', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object('step', 'CAPTURE_REFERENCE', 'kind', 'UI',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-receive-reference'),
                    jsonb_build_object('step', 'LOOKUP_REMITTANCE', 'kind', 'CAPABILITY',
                                       'capability', 'LOOKUP_REMITTANCE'),
                    jsonb_build_object('step', 'CAPTURE_BENEFICIARY_ID', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-receive-id'),
                    jsonb_build_object('step', 'CAPTURE_ACCOUNT_CONFIRM', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-receive-account'),
                    jsonb_build_object('step', 'VALIDATE_PAYOUT', 'kind', 'CAPABILITY',
                                       'capability', 'VALIDATE_PAYOUT'),
                    jsonb_build_object('step', 'CONFIRM_PAYOUT', 'kind', 'UI',
                                       'formSource', 'RECAP', 'formKey', 'product-recap'),
                    jsonb_build_object('step', 'EXECUTE_PAYOUT', 'kind', 'INVOKE', 'actorRole', 'PROVIDER',
                                       'capability', 'EXECUTE_PAYOUT', 'operation', 'PAYOUT'),
                    jsonb_build_object('step', 'COMPLETE_PAYOUT', 'kind', 'CAPABILITY',
                                       'capability', 'COMPLETE_PAYOUT'),
                    jsonb_build_object('step', 'NOTIFY', 'kind', 'CAPABILITY', 'capability', 'NOTIFY')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'remittance-receive',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'participantKind', 'REMITTANCE',
                            'serviceType', 'TRANSFERT'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'REMITTANCE_RECEIVE',
                            'participantKind', 'RESEAU',
                            'serviceType', 'TRANSFERT'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'remittanceReference', 'mtcn', 'reference', 'beneficiaryIdNumber',
                            'beneficiaryIdType', 'payoutAccount'
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Confirmation remise remittance',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'remittanceReference', 'label', 'Référence'),
                            jsonb_build_object('key', 'payoutMethod', 'label', 'Mode'),
                            jsonb_build_object('key', 'beneficiaryName', 'label', 'Bénéficiaire'),
                            jsonb_build_object('key', 'receiveAmount', 'label', 'Montant à remettre'),
                            jsonb_build_object('key', 'receiveCurrency', 'label', 'Devise'),
                            jsonb_build_object('key', 'providerCode', 'label', 'Opérateur'),
                            jsonb_build_object('key', 'payoutAccount', 'label', 'Compte'),
                            jsonb_build_object('key', 'beneficiaryIdNumber', 'label', 'N° pièce')
                    )
            ),
            'softRules', jsonb_build_array(
                    jsonb_build_object(
                            'step', 'VALIDATE_PAYOUT',
                            'type', 'REQUIRED_FIELD',
                            'params', jsonb_build_object(
                                    'field', 'remittanceReference',
                                    'errorCode', 'RECEIVE_REF_REQUIRED'
                            )
                    ),
                    jsonb_build_object(
                            'step', 'VALIDATE_PAYOUT',
                            'type', 'REQUIRED_FIELD',
                            'params', jsonb_build_object(
                                    'field', 'payoutMethod',
                                    'errorCode', 'RECEIVE_METHOD_REQUIRED'
                            )
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE_RECEIVE';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE_RECEIVE'
  AND v.version = '1.0';
