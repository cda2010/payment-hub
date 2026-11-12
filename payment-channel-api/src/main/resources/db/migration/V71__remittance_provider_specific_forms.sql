-- Formulaires spécifiques RIA / WESTERN_UNION (send + receive)
-- + actorRole PROVIDER sur les étapes post-sélection pour résoudre le binding

-- Remettre SELECT_PROVIDER tôt dans steps + actorRole PROVIDER sur écrans métier
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{steps}',
        jsonb_build_array(
                'CAPTURE_SENDER',
                'SELECT_PROVIDER',
                'CAPTURE_BENEFICIARY',
                'SELECT_COUNTRY',
                'SELECT_PAYOUT_METHOD',
                'CAPTURE_AMOUNT',
                'VALIDATE_AMOUNT',
                'GET_QUOTE',
                'RESOLVE_DOCUMENTS',
                'UPLOAD_DOCUMENTS',
                'VALIDATE_DOCUMENTS',
                'CONFIRM',
                'SELECT_FUNDING',
                'AUTHENTICATE',
                'EXECUTE_REMITTANCE',
                'NOTIFY',
                'WAIT_PAYOUT'
        )
                    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE';

-- Bindings provider-specific (idempotent)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb)
            || jsonb_build_array(
                -- shared SELECT_PROVIDER (déjà présent) ; écrans métier RIA
                jsonb_build_object(
                        'step', 'CAPTURE_BENEFICIARY', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'RIA', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-beneficiary-ria'),
                jsonb_build_object(
                        'step', 'SELECT_COUNTRY', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'RIA', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-country-ria'),
                jsonb_build_object(
                        'step', 'SELECT_PAYOUT_METHOD', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'RIA', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-payout-ria'),
                jsonb_build_object(
                        'step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'RIA', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-amount-ria'),
                -- WESTERN_UNION
                jsonb_build_object(
                        'step', 'CAPTURE_BENEFICIARY', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'WESTERN_UNION', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-beneficiary-western-union'),
                jsonb_build_object(
                        'step', 'SELECT_COUNTRY', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'WESTERN_UNION', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-country-western-union'),
                jsonb_build_object(
                        'step', 'SELECT_PAYOUT_METHOD', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'WESTERN_UNION', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-payout-western-union'),
                jsonb_build_object(
                        'step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'WESTERN_UNION', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-amount-western-union')
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'providerCode' = 'RIA'
          AND b ->> 'step' = 'CAPTURE_BENEFICIARY'
          AND b ->> 'formKey' = 'remittance-beneficiary-ria'
    );

-- Fallback shared : actorRole PROVIDER pour résoudre roleAssignments
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{bindings}',
        (
            SELECT jsonb_agg(
                           CASE
                               WHEN b ->> 'step' IN (
                                                     'CAPTURE_BENEFICIARY',
                                                     'SELECT_COUNTRY',
                                                     'SELECT_PAYOUT_METHOD',
                                                     'CAPTURE_AMOUNT'
                                   )
                                   AND (b ->> 'providerCode') IS NULL
                                   THEN b || jsonb_build_object('actorRole', 'PROVIDER')
                               ELSE b
                               END
                   )
            FROM jsonb_array_elements(COALESCE(configuration -> 'bindings', '[]'::jsonb)) b
        )
                    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE';

-- Receive : formulaires ID provider-specific
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object(
                        'step', 'CAPTURE_BENEFICIARY_ID', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'RIA', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-receive-id-ria'),
                jsonb_build_object(
                        'step', 'CAPTURE_BENEFICIARY_ID', 'kind', 'UI', 'actorRole', 'PROVIDER',
                        'providerCode', 'WESTERN_UNION', 'formSource', 'FORM_KEY',
                        'formKey', 'remittance-receive-id-western-union')
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE_RECEIVE'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'providerCode' = 'RIA'
          AND b ->> 'step' = 'CAPTURE_BENEFICIARY_ID'
          AND b ->> 'formKey' = 'remittance-receive-id-ria'
    );

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{bindings}',
        (
            SELECT jsonb_agg(
                           CASE
                               WHEN b ->> 'step' = 'CAPTURE_BENEFICIARY_ID'
                                   AND (b ->> 'providerCode') IS NULL
                                   THEN b || jsonb_build_object('actorRole', 'PROVIDER')
                               ELSE b
                               END
                   )
            FROM jsonb_array_elements(COALESCE(configuration -> 'bindings', '[]'::jsonb)) b
        )
                    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE_RECEIVE';
