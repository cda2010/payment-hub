-- Remittance providers métier : RIA + WESTERN_UNION uniquement

UPDATE payment_provider
SET actif = FALSE,
    updated_at = NOW()
WHERE code = 'MONEYGRAM';

UPDATE payment_provider
SET participant_kind = 'REMITTANCE',
    actif = TRUE,
    updated_at = NOW()
WHERE code IN ('RIA', 'WESTERN_UNION');

UPDATE payment_provider
SET libelle = 'RIA Money Transfer',
    description = 'Opérateur remittance RIA',
    updated_at = NOW()
WHERE code = 'RIA';

UPDATE payment_provider
SET libelle = 'Western Union',
    description = 'Opérateur remittance Western Union',
    updated_at = NOW()
WHERE code = 'WESTERN_UNION';

-- Participants PROVIDER = RIA (défaut) + WESTERN_UNION (autres rôles inchangés)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{participants}',
        (
            SELECT COALESCE(jsonb_agg(elem ORDER BY ord), '[]'::jsonb)
            FROM (
                     SELECT p AS elem, 0 AS ord
                     FROM jsonb_array_elements(COALESCE(v.configuration -> 'participants', '[]'::jsonb)) p
                     WHERE p ->> 'role' IS DISTINCT FROM 'PROVIDER'
                     UNION ALL
                     SELECT jsonb_build_object(
                                    'role', 'PROVIDER',
                                    'providerCode', 'RIA',
                                    'default', true
                            ), 1
                     UNION ALL
                     SELECT jsonb_build_object(
                                    'role', 'PROVIDER',
                                    'providerCode', 'WESTERN_UNION',
                                    'default', false
                            ), 2
                 ) x
        )
                    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code IN ('REMITTANCE', 'REMITTANCE_RECEIVE');
