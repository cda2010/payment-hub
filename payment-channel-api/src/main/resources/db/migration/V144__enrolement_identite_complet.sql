-- Complète l'identité d'ouverture : lieu, sexe, nationalité, ville,
-- numéro et validité de la pièce, plus le selfie.
DO $$
DECLARE
    cfg jsonb;
    entree record;
    docs jsonb;
BEGIN
    SELECT v.configuration
    INTO cfg
    FROM catalog_de_service s
    JOIN catalog_de_service_version v ON v.id = s.current_version_id
    WHERE s.code = 'ENROLEMENT';

    IF cfg IS NULL THEN
        RETURN;
    END IF;

    FOR entree IN
        SELECT *
        FROM (VALUES
            ('BF', 'Numéro de la CNIB'),
            ('BJ', 'Numéro de la pièce'),
            ('CI', 'Numéro de la CNI'),
            ('SN', 'Numéro de la CNI'),
            ('ML', 'Numéro NINA'),
            ('NE', 'Numéro de la pièce'),
            ('TG', 'Numéro de la pièce'),
            ('GW', 'Numéro de la pièce'),
            ('GN', 'Numéro de la pièce'),
            ('MA', 'Numéro CIN'),
            ('TD', 'Numéro de la pièce')
        ) AS pays(code, piece)
    LOOP
        IF cfg #> ARRAY['pays', entree.code] IS NULL THEN
            CONTINUE;
        END IF;
        cfg := jsonb_set(
            cfg,
            ARRAY['pays', entree.code, 'ouverture', 'champs'],
            jsonb_build_array(
                jsonb_build_object('code', 'nom', 'libelle', 'Nom', 'type', 'TEXT', 'requis', true),
                jsonb_build_object('code', 'prenom', 'libelle', 'Prénom', 'type', 'TEXT', 'requis', true),
                jsonb_build_object('code', 'dateNaissance', 'libelle', 'Date de naissance', 'type', 'DATE', 'requis', true),
                jsonb_build_object('code', 'lieuNaissance', 'libelle', 'Lieu de naissance', 'type', 'TEXT', 'requis', true),
                jsonb_build_object('code', 'sexe', 'libelle', 'Sexe', 'type', 'CHOICE', 'requis', true),
                jsonb_build_object('code', 'nationalite', 'libelle', 'Nationalité', 'type', 'CHOICE', 'requis', true),
                jsonb_build_object('code', 'ville', 'libelle', 'Ville', 'type', 'TEXT', 'requis', true),
                jsonb_build_object('code', 'numeroPiece', 'libelle', entree.piece, 'type', 'TEXT', 'requis', true),
                jsonb_build_object('code', 'dateValidite', 'libelle', 'Fin de validité de la pièce', 'type', 'DATE', 'requis', true)
            )
        );
        docs := cfg #> ARRAY['pays', entree.code, 'ouverture', 'documents'];
        IF docs IS NULL OR NOT EXISTS (
            SELECT 1 FROM jsonb_array_elements(docs) document WHERE document->>'face' = 'SELFIE'
        ) THEN
            cfg := jsonb_set(
                cfg,
                ARRAY['pays', entree.code, 'ouverture', 'documents'],
                COALESCE(docs, '[]'::jsonb) || jsonb_build_array(
                    jsonb_build_object('face', 'SELFIE', 'libelle', 'Selfie', 'requis', true)
                )
            );
        END IF;
    END LOOP;

    UPDATE catalog_de_service_version v
    SET configuration = cfg
    FROM catalog_de_service s
    WHERE s.code = 'ENROLEMENT'
      AND v.id = s.current_version_id;
END $$;
