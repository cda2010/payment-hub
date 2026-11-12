-- CASH_OUT : parcours client-initié → code retrait agent (pratique marché MM / banques)
-- Corrige le vocabulaire « moyen de paiement » → provider source + canal agent

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'CASH_OUT',
       'Cash-out (orchestration)',
       'Hôte technique du parcours catalogue CASH_OUT',
       TRUE,
       92,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'CASH_OUT');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'CASH_OUT'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'TRANSFERT',
       'Cash-out agent',
       'TRANSFERT',
       'cashOut',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'CASH_OUT'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'cashOut',
    nom = 'Cash-out agent',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'CASH_OUT'
  AND f.service_type = 'TRANSFERT';

-- Réaligner la version catalogue CASH_OUT (déjà seedée V38/V39)
UPDATE catalog_de_service_version v
SET workflow_key = 'cashOut',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'terminalOnConfirm', false,
            'minAmount', 500,
            'defaultFee', 100,
            'steps', jsonb_build_array(
                    'CAPTURE_AMOUNT',
                    'SELECT_SOURCE',
                    'SELECT_AGENT_CHANNEL',
                    'CALCULATE_FEES',
                    'CONFIRM',
                    'AUTHENTICATE',
                    'EXECUTE_CASHOUT',
                    'GENERATE_VOUCHER',
                    'WAIT_AGENT_PAYOUT',
                    'NOTIFY'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'SOURCE',
                            'label', 'Compte / wallet à débiter',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    ),
                    jsonb_build_object(
                            'code', 'AGENT_NETWORK',
                            'label', 'Réseau agent (remise cash)',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'WAVE', 'default', true),
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_MONEY', 'default', false),
                    jsonb_build_object('role', 'AGENT_NETWORK', 'providerCode', 'WAVE', 'default', true),
                    jsonb_build_object('role', 'AGENT_NETWORK', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'AGENT_NETWORK', 'providerCode', 'CORIS_MONEY', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object('step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-out-amount'),
                    jsonb_build_object('step', 'SELECT_SOURCE', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-out-source'),
                    jsonb_build_object('step', 'SELECT_AGENT_CHANNEL', 'kind', 'UI', 'actorRole', 'AGENT_NETWORK',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-out-agent'),
                    jsonb_build_object('step', 'CALCULATE_FEES', 'kind', 'SOFT', 'operation', 'CALCULATE_FEES'),
                    jsonb_build_object('step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'cash-out-confirm'),
                    jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                       'operation', 'GENERATE_OTP'),
                    jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                    jsonb_build_object('step', 'EXECUTE_CASHOUT', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                       'operation', 'TRANSFERT'),
                    jsonb_build_object('step', 'GENERATE_VOUCHER', 'kind', 'SOFT', 'operation', 'GENERATE_VOUCHER'),
                    jsonb_build_object('step', 'WAIT_AGENT_PAYOUT', 'kind', 'WAIT'),
                    jsonb_build_object('step', 'NOTIFY', 'kind', 'SOFT', 'operation', 'NOTIFY')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'cash-out',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'participantKind', 'WALLET',
                            'serviceType', 'TRANSFERT'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'CASH_OUT',
                            'participantKind', 'RESEAU'
                    ),
                    'copyRequestFields', jsonb_build_array('agentNetwork', 'agentCode', 'sourceAccount', 'phoneNumber')
            ),
            'softRules', jsonb_build_array(
                    jsonb_build_object(
                            'step', 'CALCULATE_FEES',
                            'type', 'MIN_AMOUNT',
                            'params', jsonb_build_object('minAmountKey', 'minAmount', 'errorCode', 'CASH_OUT_MIN_AMOUNT')
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
                                    'field', 'cashOutCode',
                                    'prefix', 'CO',
                                    'length', 8
                            )
                    ),
                    jsonb_build_object(
                            'step', 'WAIT_AGENT_PAYOUT',
                            'type', 'PASS',
                            'params', jsonb_build_object(
                                    'message', 'En attente de remise cash par l''agent'
                            )
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.version = '1.0';
