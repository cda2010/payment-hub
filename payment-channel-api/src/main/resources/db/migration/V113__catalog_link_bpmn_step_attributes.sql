-- LINK : les serviceTasks déclarent leur capability dans le BPMN (pay:capability / pay:actorRole / pay:operation).
-- La configuration catalogue ne garde que les bindings UI ; softRules et paramètres métier inchangés.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(
                (SELECT jsonb_agg(b ORDER BY ord)
                 FROM jsonb_array_elements(v.configuration -> 'bindings') WITH ORDINALITY AS t(b, ord)
                 WHERE b ->> 'kind' = 'UI'),
                '[]'::jsonb
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'LINK'
  AND v.workflow_key = 'accountLink'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';
