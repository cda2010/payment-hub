-- Upgrade MARKETPLACE : même moteur checkout (PAYER + OTP) que COFFRE / facture.

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'MARKETPLACE',
       'Marketplace (orchestration)',
       'Hôte technique du parcours catalogue MARKETPLACE',
       TRUE,
       56,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'MARKETPLACE');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAYEMENT'
FROM payment_provider p
WHERE p.code = 'MARKETPLACE'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'MARKETPLACE',
       'Achat marketplace',
       'PAYEMENT',
       'merchantCheckout',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'MARKETPLACE'
  AND NOT EXISTS (
        SELECT 1 FROM payment_flow f
        WHERE f.provider_id = p.id AND f.service_type = 'PAYEMENT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'merchantCheckout',
    nom = 'Achat marketplace',
    code = 'MARKETPLACE',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'MARKETPLACE'
  AND f.service_type = 'PAYEMENT';

UPDATE catalog_de_service_version v
SET workflow_key = 'merchantCheckout',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'PAYEMENT',
            'journeyCode', 'MARKETPLACE_BUY',
            'supportedChannels', jsonb_build_array('MOBILE'),
            'defaultChannel', 'MOBILE',
            'channelDefaults', jsonb_build_object('MOBILE', jsonb_build_object()),
            'terminalOnConfirm', true,
            'steps', jsonb_build_array(
                    'SELECT_PROVIDER',
                    'AUTHENTICATE',
                    'EXECUTE_PAYMENT',
                    'NOTIFY'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'PAYER',
                            'label', 'Compte à débiter',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'CORIS_MONEY', 'default', true),
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'WAVE', 'default', false),
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'CORIS_BANK', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object('step', 'selectPayProvider', 'kind', 'UI', 'actorRole', 'PAYER',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-bill-select-pay'),
                    jsonb_build_object('step', 'SELECT_PROVIDER', 'kind', 'UI', 'actorRole', 'PAYER',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-bill-select-pay'),
                    jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                                       'capability', 'GENERATE_OTP', 'operation', 'GENERATE_OTP'),
                    jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'PAYER',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                    jsonb_build_object('step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                                       'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                    jsonb_build_object(
                            'step', 'enterOtp', 'kind', 'UI', 'actorRole', 'PAYER',
                            'providerCode', 'CORIS_MONEY', 'formSource', 'FORM_KEY',
                            'formKey', 'provider-otp-code'),
                    jsonb_build_object(
                            'step', 'enterOtp', 'kind', 'UI', 'actorRole', 'PAYER',
                            'providerCode', 'ORANGE_MONEY', 'formSource', 'FORM_KEY',
                            'formKey', 'provider-otp-code'),
                    jsonb_build_object(
                            'step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                            'providerCode', 'CORIS_MONEY',
                            'capability', 'GENERATE_OTP', 'operation', 'GENERATE_OTP'),
                    jsonb_build_object(
                            'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                            'providerCode', 'CORIS_MONEY',
                            'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                    jsonb_build_object(
                            'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                            'providerCode', 'ORANGE_MONEY',
                            'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                    jsonb_build_object(
                            'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                            'providerCode', 'WAVE',
                            'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                    jsonb_build_object(
                            'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                            'providerCode', 'CORIS_BANK',
                            'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'marketplace',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object('serviceType', 'PAYEMENT'),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'MARKETPLACE',
                            'participantKind', 'RESEAU',
                            'serviceType', 'PAYEMENT'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'phoneNumber', 'customerReference', 'amount', 'currency',
                            'payProvider', 'payProviderCode',
                            'offerId', 'offerTitle', 'merchant', 'offerCategory'
                    )
            ),
            'presentation', jsonb_build_object(
                    'version', 1,
                    'channel', 'MOBILE',
                    'sections', jsonb_build_array(
                            jsonb_build_object(
                                    'id', 'payMethods',
                                    'displayKind', 'SHORTCUT_SLIDE',
                                    'dataSource', 'CATALOG_PARTICIPANTS',
                                    'origin', 'LOCAL',
                                    'title', 'Moyen de paiement',
                                    'bindsTo', 'payProvider',
                                    'params', jsonb_build_object('role', 'PAYER')
                            )
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif achat',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'payProvider', 'label', 'Moyen de paiement'),
                            jsonb_build_object('key', 'offerTitle', 'label', 'Offre'),
                            jsonb_build_object('key', 'merchant', 'label', 'Marchand'),
                            jsonb_build_object('key', 'customerReference', 'label', 'Référence')
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'MARKETPLACE';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW(),
    description = 'Offres partenaires payables depuis le wallet (checkout gateway)'
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'MARKETPLACE'
  AND v.version = '1.0';
