-- Page d'adresse structurée. La ville et l'adresse libre quittent l'identité.
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        configuration,
        '{steps}',
        CASE
            WHEN configuration->'steps' ? 'ADRESSE' THEN configuration->'steps'
            ELSE configuration->'steps' || '["ADRESSE"]'::jsonb
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
        '[{"kind":"UI","step":"ADRESSE","formKey":"enrolement-adresse","formSource":"FORM_KEY"}]'::jsonb
    )
FROM catalog_de_service s
WHERE v.id = s.current_version_id
  AND s.code = 'ENROLEMENT'
  AND NOT EXISTS (
      SELECT 1
      FROM jsonb_array_elements(v.configuration->'bindings') b
      WHERE b->>'step' = 'ADRESSE' AND b->>'formKey' = 'enrolement-adresse'
  );

DO $$
DECLARE
    cfg jsonb;
    entree record;
    champs jsonb;
BEGIN
    SELECT v.configuration
    INTO cfg
    FROM catalog_de_service s
    JOIN catalog_de_service_version v ON v.id = s.current_version_id
    WHERE s.code = 'ENROLEMENT';

    IF cfg IS NULL OR cfg -> 'pays' IS NULL THEN
        RETURN;
    END IF;

    FOR entree IN
        SELECT cle AS code
        FROM jsonb_object_keys(cfg -> 'pays') AS cle
    LOOP
        champs := cfg #> ARRAY['pays', entree.code, 'ouverture', 'champs'];
        IF champs IS NULL THEN
            CONTINUE;
        END IF;
        cfg := jsonb_set(
            cfg,
            ARRAY['pays', entree.code, 'ouverture', 'champs'],
            (
                SELECT COALESCE(jsonb_agg(champ), '[]'::jsonb)
                FROM jsonb_array_elements(champs) champ
                WHERE champ ->> 'code' NOT IN ('ville', 'adresse')
            )
        );
    END LOOP;

    UPDATE catalog_de_service_version v
    SET configuration = cfg
    FROM catalog_de_service s
    WHERE s.code = 'ENROLEMENT'
      AND v.id = s.current_version_id;
END $$;
