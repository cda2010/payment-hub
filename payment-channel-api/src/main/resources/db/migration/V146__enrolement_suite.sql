-- Écran « créer le compte maintenant / identifiez-vous » du produit ENROLEMENT.
-- Idempotent : le binding n'est ajouté qu'une fois.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{steps}',
        CASE
            WHEN configuration->'steps' ? 'SUITE' THEN configuration->'steps'
            ELSE configuration->'steps' || '["SUITE"]'::jsonb
        END
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT';

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{bindings}',
        configuration->'bindings' ||
        '[{"kind":"UI","step":"SUITE","formKey":"enrolement-suite","formSource":"FORM_KEY"}]'::jsonb
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT'
  AND NOT EXISTS (
      SELECT 1
      FROM jsonb_array_elements(v.configuration->'bindings') b
      WHERE b->>'step' = 'SUITE' AND b->>'formKey' = 'enrolement-suite'
  );
