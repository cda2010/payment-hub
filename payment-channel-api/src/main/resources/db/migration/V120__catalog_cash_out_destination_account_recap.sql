-- CASH_OUT récap : afficher le compte destination agent (alias UNITE / externalRef).

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{recap,fields}',
        (
            SELECT COALESCE(jsonb_agg(elem ORDER BY ord), '[]'::jsonb)
            FROM (
                     SELECT f AS elem, ord
                     FROM jsonb_array_elements(v.configuration -> 'recap' -> 'fields')
                              WITH ORDINALITY AS t(f, ord)
                     WHERE f->>'key' <> 'destinationAccount'
                     UNION ALL
                     SELECT jsonb_build_object('key', 'destinationAccount', 'label', 'Compte agent (DEST)'),
                            1000
                 ) x
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut'
  AND jsonb_typeof(v.configuration -> 'recap' -> 'fields') = 'array';
