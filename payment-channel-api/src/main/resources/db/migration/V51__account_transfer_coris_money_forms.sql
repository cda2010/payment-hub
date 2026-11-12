-- ACCOUNT_TRANSFER : formulaires contextuels CORIS_MONEY (convention + override providerCode)
-- formKeys : account-transfer-source-coris-money / account-transfer-destination-coris-money

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object(
                        'step', 'SELECT_SOURCE_ACCOUNT',
                        'kind', 'UI',
                        'actorRole', 'SOURCE',
                        'providerCode', 'CORIS_MONEY',
                        'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-source-coris-money'
                ),
                jsonb_build_object(
                        'step', 'CAPTURE_DESTINATION',
                        'kind', 'UI',
                        'actorRole', 'DESTINATION',
                        'providerCode', 'CORIS_MONEY',
                        'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-destination-coris-money'
                )
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'providerCode' = 'CORIS_MONEY'
          AND b ->> 'step' = 'SELECT_SOURCE_ACCOUNT'
          AND b ->> 'formKey' = 'account-transfer-source-coris-money'
    );
