-- CASH_OUT : autoriser le compte bancaire Coris comme SOURCE
-- (en plus des wallets), pour pouvoir changer de compte à débiter
-- côté mobile comme sur ACCOUNT_TRANSFER.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{participants}',
        COALESCE(v.configuration -> 'participants', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_BANK', 'default', false)
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'participants', '[]'::jsonb)) p
        WHERE p ->> 'providerCode' = 'CORIS_BANK'
          AND p ->> 'role' = 'SOURCE'
    );

-- providersQuery : wallets + banques (plus seulement WALLET)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{gateway,providersQuery}',
        jsonb_build_object('serviceType', 'TRANSFERT')
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND (v.configuration -> 'gateway' -> 'providersQuery' ->> 'participantKind') = 'WALLET';
