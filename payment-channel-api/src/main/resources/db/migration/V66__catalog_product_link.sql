-- LINK (alias REGISTER) : rattachement compte wallet — ServiceType LINK

INSERT INTO categorie_service (id, code, name, description, ordre_affichage, actif, created_at, updated_at)
SELECT gen_random_uuid(), 'ACCOUNT', 'Compte', 'Liaison / déliaison de comptes', 45, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM categorie_service WHERE code = 'ACCOUNT');

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'LINK',
       'Liaison de compte',
       'Rattachement d''un compte wallet / MSISDN (register)',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'ACCOUNT'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'LINK');

-- Version 1.0 si absente
INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       'accountLink',
       '{}'::jsonb,
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'LINK'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'LINK',
       'Link (orchestration)',
       'Hôte technique du parcours catalogue LINK',
       TRUE,
       90,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'LINK');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'LINK'
FROM payment_provider p
WHERE p.code = 'LINK'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'LINK',
       'Liaison compte',
       'LINK',
       'accountLink',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'LINK'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'LINK'
  );

UPDATE payment_flow f
SET bpm_process_key = 'accountLink',
    nom = 'Liaison compte',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'LINK'
  AND f.service_type = 'LINK';

UPDATE catalog_de_service_version v
SET workflow_key = 'accountLink',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'LINK',
            'terminalOnConfirm', false,
            'steps', jsonb_build_array(
                    'SELECT_PROVIDER',
                    'CAPTURE_ACCOUNT',
                    'VALIDATE_ACCOUNT',
                    'CONFIRM',
                    'AUTHENTICATE',
                    'EXECUTE_LINK',
                    'NOTIFY'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'SOURCE',
                            'label', 'Wallet / compte à rattacher',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'WAVE', 'default', true),
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_MONEY', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object('step', 'SELECT_PROVIDER', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'account-link-provider'),
                    jsonb_build_object('step', 'CAPTURE_ACCOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'account-link-account'),
                    jsonb_build_object('step', 'VALIDATE_ACCOUNT', 'kind', 'CAPABILITY',
                                       'capability', 'VALIDATE_ACCOUNT'),
                    jsonb_build_object('step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'RECAP', 'formKey', 'product-recap'),
                    jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                       'capability', 'GENERATE_OTP', 'operation', 'GENERATE_OTP'),
                    jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                    jsonb_build_object('step', 'EXECUTE_LINK', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                       'capability', 'EXECUTE_LINK', 'operation', 'LINK'),
                    jsonb_build_object('step', 'NOTIFY', 'kind', 'CAPABILITY', 'capability', 'NOTIFY')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'link',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'participantKind', 'WALLET',
                            'serviceType', 'LINK'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'LINK',
                            'participantKind', 'RESEAU',
                            'serviceType', 'LINK'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'phoneNumber', 'accountAlias', 'customerReference', 'providerRail'
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif liaison de compte',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'providerRail', 'label', 'Wallet'),
                            jsonb_build_object('key', 'phoneNumber', 'label', 'Numéro'),
                            jsonb_build_object('key', 'accountAlias', 'label', 'Alias'),
                            jsonb_build_object('key', 'customerReference', 'label', 'Référence client')
                    )
            ),
            'softRules', jsonb_build_array(
                    jsonb_build_object(
                            'step', 'VALIDATE_ACCOUNT',
                            'type', 'REQUIRED_FIELD',
                            'params', jsonb_build_object('field', 'phoneNumber', 'errorCode', 'LINK_PHONE_REQUIRED')
                    ),
                    jsonb_build_object(
                            'step', 'VALIDATE_ACCOUNT',
                            'type', 'REGEX_FIELD',
                            'params', jsonb_build_object(
                                    'field', 'phoneNumber',
                                    'pattern', '^[0-9+]{8,15}$',
                                    'errorCode', 'LINK_PHONE_INVALID'
                            )
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'LINK';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'LINK'
  AND v.version = '1.0';
