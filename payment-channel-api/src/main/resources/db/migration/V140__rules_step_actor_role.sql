-- Le contrôle des plafonds suit le rôle déjà porté par l'étape.
-- Cash-in et cash-out : la personne de l'opération (DESTINATION / SOURCE).
-- Le transfert reçu garde DESTINATION sur CHECK_LIMITS (V139).

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        (
            SELECT jsonb_agg(
                CASE
                    WHEN elem->>'step' = 'EVALUATE_RULES'
                        THEN elem || jsonb_build_object('actorRole', 'DESTINATION')
                    ELSE elem
                END
            )
            FROM jsonb_array_elements(v.configuration -> 'bindings') elem
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_IN'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        (
            SELECT jsonb_agg(
                CASE
                    WHEN elem->>'step' = 'EVALUATE_RULES'
                        THEN elem || jsonb_build_object('actorRole', 'SOURCE')
                    ELSE elem
                END
            )
            FROM jsonb_array_elements(v.configuration -> 'bindings') elem
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';
