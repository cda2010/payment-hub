-- Presentation catalogue ACCOUNT_TRANSFER : le produit décrit lui-même la liste
-- des réseaux à créditer et son mode d'affichage, au lieu de laisser le mobile
-- composer la liste depuis /gateway/transfer-destinations.
--
-- Les items viennent du référentiel transfer_participant (dataSource LOCAL
-- TRANSFER_PARTICIPANTS, filtré sur le rôle DESTINATION) : pas de duplication
-- du référentiel dans le JSON du produit.

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
                                'title', 'Destination',
                                'bindsTo', 'destinationProvider',
                                'params', jsonb_build_object('role', 'DESTINATION')
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND (v.configuration -> 'presentation') IS NULL;
