-- Compte Coris Money : GETINFOS part vers l'omnicanal par payment-2, juste après le oui.
-- Idempotent.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{steps}',
        (
            SELECT COALESCE(jsonb_agg(to_jsonb(step) ORDER BY rang), '[]'::jsonb)
            FROM (
                SELECT step, rang
                FROM (
                    SELECT elem #>> '{}' AS step, ord::numeric AS rang
                    FROM jsonb_array_elements(v.configuration->'steps') WITH ORDINALITY AS t(elem, ord)
                    WHERE elem #>> '{}' <> 'GETINFOS'
                ) presents
                UNION ALL
                SELECT 'GETINFOS',
                       COALESCE(
                           (
                               SELECT ord::numeric
                               FROM jsonb_array_elements(v.configuration->'steps') WITH ORDINALITY AS t(elem, ord)
                               WHERE elem #>> '{}' = 'PORTEFEUILLE'
                           ),
                           0
                       ) + 0.5
            ) ordered
        )
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT';

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{bindings}',
        configuration->'bindings' ||
        '[{"kind":"CAPABILITY","step":"GETINFOS","capability":"GET_INFOS","operation":"GETINFOS"}]'::jsonb
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT'
  AND NOT EXISTS (
      SELECT 1
      FROM jsonb_array_elements(v.configuration->'bindings') b
      WHERE b->>'step' = 'GETINFOS' AND b->>'capability' = 'GET_INFOS'
  );
