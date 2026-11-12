-- Presentation catalogue RTGS : destinations banques (même contrat qu'ACCOUNT_TRANSFER V88).
-- Les items viennent du référentiel transfer_participant (rôle DESTINATION).

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'presentation', jsonb_build_object(
                'version', 1,
                'sections', jsonb_build_array(
                        jsonb_build_object(
                                'id', 'destinations',
                                'displayKind', 'SHORTCUT_SLIDE',
                                'dataSource', 'TRANSFER_PARTICIPANTS',
                                'origin', 'LOCAL',
                                'title', 'Banque destinataire',
                                'bindsTo', 'destinationProvider',
                                'params', jsonb_build_object('role', 'DESTINATION')
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'RTGS'
  AND (v.configuration -> 'presentation') IS NULL;
