-- Produit catalogue CHECKOUT (paiement marchand) — PUBLISHED
-- Hôte technique + BPMN merchantCheckout → callActivity wallet (OTP / redirect)

INSERT INTO categorie_service (id, code, name, description, ordre_affichage, actif, created_at, updated_at)
SELECT gen_random_uuid(), 'PAYMENT', 'Paiement', 'Checkout / paiement marchand', 25, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM categorie_service WHERE code = 'PAYMENT');

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'CHECKOUT',
       'Checkout',
       'Paiement marchand (wallet / carte) — OTP ou redirect selon le rail',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'PAYMENT'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'CHECKOUT');

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
WHERE s.code = 'CHECKOUT'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'CHECKOUT',
       'Checkout (orchestration)',
       'Hôte technique du parcours catalogue CHECKOUT',
       TRUE,
       85,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'CHECKOUT');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAYEMENT'
FROM payment_provider p
WHERE p.code = 'CHECKOUT'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'CHECKOUT',
       'Checkout marchand',
       'PAYEMENT',
       'merchantCheckout',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'CHECKOUT'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'PAYEMENT'
  );

UPDATE payment_flow f
SET bpm_process_key = 'merchantCheckout',
    nom = 'Checkout marchand',
    code = 'CHECKOUT',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'CHECKOUT'
  AND f.service_type = 'PAYEMENT';

UPDATE catalog_de_service_version v
SET workflow_key = 'merchantCheckout',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'PAYEMENT',
            'journeyCode', 'STANDARD_CHECKOUT',
            'supportedChannels', jsonb_build_array('MOBILE', 'GAB', 'USSD', 'BACKOFFICE'),
            'defaultChannel', 'MOBILE',
            'channelDefaults', jsonb_build_object(
                    'MOBILE', jsonb_build_object(),
                    'GAB', jsonb_build_object(),
                    'USSD', jsonb_build_object(),
                    'BACKOFFICE', jsonb_build_object()
            ),
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
                            'label', 'Payeur (wallet / carte)',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'CORIS_MONEY', 'default', true),
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'WAVE', 'default', false),
                    jsonb_build_object('role', 'PAYER', 'providerCode', 'CARD_PAYMENT', 'default', false)
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
                                       'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'checkout',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'serviceType', 'PAYEMENT'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'CHECKOUT',
                            'participantKind', 'RESEAU',
                            'serviceType', 'PAYEMENT'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'phoneNumber', 'customerReference', 'amount', 'currency', 'payProvider', 'payProviderCode'
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif checkout',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'payProvider', 'label', 'Moyen de paiement'),
                            jsonb_build_object('key', 'phoneNumber', 'label', 'Téléphone'),
                            jsonb_build_object('key', 'customerReference', 'label', 'Référence')
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CHECKOUT';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CHECKOUT'
  AND v.version = '1.0';
