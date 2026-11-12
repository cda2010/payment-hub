-- Ajoute l'adresse à l'identité d'ouverture de chaque pays.
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
        IF champs IS NULL OR EXISTS (
            SELECT 1
            FROM jsonb_array_elements(champs) champ
            WHERE champ ->> 'code' = 'adresse'
        ) THEN
            CONTINUE;
        END IF;
        cfg := jsonb_set(
            cfg,
            ARRAY['pays', entree.code, 'ouverture', 'champs'],
            champs || jsonb_build_array(
                jsonb_build_object('code', 'adresse', 'libelle', 'Adresse', 'type', 'TEXT', 'requis', true)
            )
        );
    END LOOP;

    UPDATE catalog_de_service_version v
    SET configuration = cfg
    FROM catalog_de_service s
    WHERE s.code = 'ENROLEMENT'
      AND v.id = s.current_version_id;
END $$;
