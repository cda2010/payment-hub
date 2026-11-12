-- Produit catalogue COFFRE (épargne) — même moteur checkout (PAYER + OTP)
-- que CHECKOUT / facture, catégorie SAVINGS.

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'COFFRE',
       'Coffre',
       'Dépôt vers un coffre d''épargne (wallet / compte)',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'SAVINGS'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'COFFRE');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       'merchantCheckout',
       '{}'::jsonb,
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'COFFRE'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'COFFRE',
       'Coffre (orchestration)',
       'Hôte technique du parcours catalogue COFFRE',
       TRUE,
       55,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'COFFRE');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAYEMENT'
FROM payment_provider p
WHERE p.code = 'COFFRE'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'COFFRE',
       'Dépôt coffre',
       'PAYEMENT',
       'merchantCheckout',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'COFFRE'
  AND NOT EXISTS (
        SELECT 1 FROM payment_flow f
        WHERE f.provider_id = p.id AND f.service_type = 'PAYEMENT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'merchantCheckout',
    nom = 'Dépôt coffre',
    code = 'COFFRE',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'COFFRE'
  AND f.service_type = 'PAYEMENT';

UPDATE catalog_de_service_version v
SET workflow_key = 'merchantCheckout',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'PAYEMENT',
            'journeyCode', 'COFFRE_DEPOSIT',
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
                    'pathAlias', 'coffre',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object('serviceType', 'PAYEMENT'),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'COFFRE',
                            'participantKind', 'RESEAU',
                            'serviceType', 'PAYEMENT'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'phoneNumber', 'customerReference', 'amount', 'currency',
                            'payProvider', 'payProviderCode', 'coffreId', 'coffreName'
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
                    'title', 'Récapitulatif dépôt coffre',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'payProvider', 'label', 'Moyen de paiement'),
                            jsonb_build_object('key', 'coffreName', 'label', 'Coffre'),
                            jsonb_build_object('key', 'customerReference', 'label', 'Référence')
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'COFFRE';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'COFFRE'
  AND v.version = '1.0';

-- CORIS_BANK déjà PAYER côté BILL/CHECKOUT (V95) — s'assurer du flow PAYEMENT.
INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAYEMENT'
FROM payment_provider p
WHERE p.code = 'CORIS_BANK'
ON CONFLICT DO NOTHING;
