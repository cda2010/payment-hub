-- CASH_IN : parcours client-initié → code dépôt agent → crédit wallet (pratique marché MM)

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'CASH_IN',
       'Cash-in (orchestration)',
       'Hôte technique du parcours catalogue CASH_IN',
       TRUE,
       91,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'CASH_IN');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'CASH_IN'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'TRANSFERT',
       'Cash-in agent',
       'TRANSFERT',
       'cashIn',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'CASH_IN'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'cashIn',
    nom = 'Cash-in agent',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'CASH_IN'
  AND f.service_type = 'TRANSFERT';

UPDATE catalog_de_service_version v
SET workflow_key = 'cashIn',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'terminalOnConfirm', false,
            'minAmount', 500,
            'defaultFee', 100,
            'steps', jsonb_build_array(
                    'CAPTURE_AMOUNT',
                    'SELECT_DESTINATION',
                    'SELECT_AGENT_CHANNEL',
                    'CALCULATE_FEES',
                    'CONFIRM',
                    'AUTHENTICATE',
                    'GENERATE_VOUCHER',
                    'WAIT_AGENT_COLLECT',
                    'EXECUTE_CASHIN',
                    'NOTIFY'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'DESTINATION',
                            'label', 'Compte / wallet à créditer',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    ),
                    jsonb_build_object(
                            'code', 'AGENT_NETWORK',
                            'label', 'Réseau agent (encaissement cash)',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'WAVE', 'default', true),
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'DESTINATION', 'providerCode', 'CORIS_MONEY', 'default', false),
                    jsonb_build_object('role', 'AGENT_NETWORK', 'providerCode', 'WAVE', 'default', true),
                    jsonb_build_object('role', 'AGENT_NETWORK', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'AGENT_NETWORK', 'providerCode', 'CORIS_MONEY', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object('step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'DESTINATION',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-in-amount'),
                    jsonb_build_object('step', 'SELECT_DESTINATION', 'kind', 'UI', 'actorRole', 'DESTINATION',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-in-destination'),
                    jsonb_build_object('step', 'SELECT_AGENT_CHANNEL', 'kind', 'UI', 'actorRole', 'AGENT_NETWORK',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-in-agent'),
                    jsonb_build_object('step', 'CALCULATE_FEES', 'kind', 'SOFT', 'operation', 'CALCULATE_FEES'),
                    jsonb_build_object('step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'DESTINATION',
                                       'formSource', 'RECAP', 'formKey', 'product-recap'),
                    jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'DESTINATION',
                                       'operation', 'GENERATE_OTP'),
                    jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'DESTINATION',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                    jsonb_build_object('step', 'GENERATE_VOUCHER', 'kind', 'SOFT', 'operation', 'GENERATE_VOUCHER'),
                    jsonb_build_object('step', 'WAIT_AGENT_COLLECT', 'kind', 'WAIT'),
                    jsonb_build_object('step', 'EXECUTE_CASHIN', 'kind', 'INVOKE', 'actorRole', 'DESTINATION',
                                       'operation', 'TRANSFERT'),
                    jsonb_build_object('step', 'NOTIFY', 'kind', 'SOFT', 'operation', 'NOTIFY')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'cash-in',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'participantKind', 'WALLET',
                            'serviceType', 'TRANSFERT'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'CASH_IN',
                            'participantKind', 'RESEAU'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'agentNetwork', 'agentCode', 'destinationAccount', 'destinationRail',
                            'phoneNumber', 'cashInCode'
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif cash-in',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant cash'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'fees', 'label', 'Frais'),
                            jsonb_build_object('key', 'destinationRail', 'label', 'Compte destination'),
                            jsonb_build_object('key', 'phoneNumber', 'label', 'Téléphone'),
                            jsonb_build_object('key', 'agentNetwork', 'label', 'Réseau agent'),
                            jsonb_build_object('key', 'agentCode', 'label', 'Code agent')
                    )
            ),
            'softRules', jsonb_build_array(
                    jsonb_build_object(
                            'step', 'CALCULATE_FEES',
                            'type', 'MIN_AMOUNT',
                            'params', jsonb_build_object('minAmountKey', 'minAmount', 'errorCode', 'CASH_IN_MIN_AMOUNT')
                    ),
                    jsonb_build_object(
                            'step', 'CALCULATE_FEES',
                            'type', 'DEFAULT_FEE',
                            'params', jsonb_build_object()
                    ),
                    jsonb_build_object(
                            'step', 'GENERATE_VOUCHER',
                            'type', 'GENERATE_CODE',
                            'params', jsonb_build_object(
                                    'field', 'cashInCode',
                                    'prefix', 'CI',
                                    'length', 8
                            )
                    ),
                    jsonb_build_object(
                            'step', 'WAIT_AGENT_COLLECT',
                            'type', 'PASS',
                            'params', jsonb_build_object(
                                    'message', 'En attente d''encaissement cash par l''agent'
                            )
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_IN';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_IN'
  AND v.version = '1.0';
