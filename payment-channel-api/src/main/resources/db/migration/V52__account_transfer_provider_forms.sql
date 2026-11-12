-- ACCOUNT_TRANSFER : formulaires contextuels WAVE / ORANGE_MONEY / CORIS_BANK
-- + participants banque pour le casting

-- Participants CORIS_BANK (SOURCE + DESTINATION) si absents
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{participants}',
        COALESCE(v.configuration -> 'participants', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_BANK', 'default', false),
                jsonb_build_object('role', 'DESTINATION', 'providerCode', 'CORIS_BANK', 'default', false)
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'participants', '[]'::jsonb)) p
        WHERE p ->> 'providerCode' = 'CORIS_BANK'
          AND p ->> 'role' = 'SOURCE'
    );

-- Bindings UI provider-specific (idempotent)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object(
                        'step', 'SELECT_SOURCE_ACCOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                        'providerCode', 'WAVE', 'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-source-wave'),
                jsonb_build_object(
                        'step', 'CAPTURE_DESTINATION', 'kind', 'UI', 'actorRole', 'DESTINATION',
                        'providerCode', 'WAVE', 'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-destination-wave'),
                jsonb_build_object(
                        'step', 'SELECT_SOURCE_ACCOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                        'providerCode', 'ORANGE_MONEY', 'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-source-orange-money'),
                jsonb_build_object(
                        'step', 'CAPTURE_DESTINATION', 'kind', 'UI', 'actorRole', 'DESTINATION',
                        'providerCode', 'ORANGE_MONEY', 'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-destination-orange-money'),
                jsonb_build_object(
                        'step', 'SELECT_SOURCE_ACCOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                        'providerCode', 'CORIS_BANK', 'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-source-coris-bank'),
                jsonb_build_object(
                        'step', 'CAPTURE_DESTINATION', 'kind', 'UI', 'actorRole', 'DESTINATION',
                        'providerCode', 'CORIS_BANK', 'formSource', 'FORM_KEY',
                        'formKey', 'account-transfer-destination-coris-bank')
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'providerCode' = 'WAVE'
          AND b ->> 'step' = 'SELECT_SOURCE_ACCOUNT'
          AND b ->> 'formKey' = 'account-transfer-source-wave'
    );
