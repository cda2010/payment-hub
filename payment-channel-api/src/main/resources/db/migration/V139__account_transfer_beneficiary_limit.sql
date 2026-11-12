-- ACCOUNT_TRANSFER / CHECK_LIMITS : plafond de détention du bénéficiaire (BCEAO art. 31).
-- Le rechargement mensuel reste sur le seul cash-in.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        (
            SELECT jsonb_agg(
                CASE
                    WHEN elem->>'step' = 'CHECK_LIMITS' THEN
                        elem || jsonb_build_object(
                            'kind', 'CAPABILITY',
                            'capability', 'EVALUATE_RULES',
                            'actorRole', 'DESTINATION',
                            'params', jsonb_build_object(
                                'groupCode', 'ACCOUNT_TRANSFER_CREDIT',
                                'transactionType', 'ACCOUNT_TRANSFER',
                                'subject', 'BENEFICIARY'
                            )
                        )
                    ELSE elem
                END
            )
            FROM jsonb_array_elements(v.configuration -> 'bindings') elem
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';
