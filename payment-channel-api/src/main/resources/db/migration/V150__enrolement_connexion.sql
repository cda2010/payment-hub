-- Étape de connexion du produit ENROLEMENT : identifiant = téléphone, mot de passe Keycloak.
-- Idempotent.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{steps}',
        configuration->'steps' || '["CONNEXION"]'::jsonb
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT'
  AND NOT EXISTS (
      SELECT 1
      FROM jsonb_array_elements(v.configuration->'steps') elem
      WHERE elem #>> '{}' = 'CONNEXION'
  );

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{bindings}',
        configuration->'bindings' ||
        '[{"kind":"UI","step":"CONNEXION","formKey":"enrolement-connexion","formSource":"FORM_KEY"}]'::jsonb
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT'
  AND NOT EXISTS (
      SELECT 1
      FROM jsonb_array_elements(v.configuration->'bindings') b
      WHERE b->>'step' = 'CONNEXION' AND b->>'formKey' = 'enrolement-connexion'
  );
