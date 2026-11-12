-- Presentation catalogue CASH_OUT : choix du canal de retrait
-- (GAB / Agent / Agence) avant le parcours gateway.
-- STATIC_ITEMS + ENTRY_GRID — bindsTo withdrawalChannel (distinct d'accessChannel).

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'presentation', jsonb_build_object(
                'version', 1,
                'sections', jsonb_build_array(
                        jsonb_build_object(
                                'id', 'withdrawal-channels',
                                'displayKind', 'ENTRY_GRID',
                                'dataSource', 'STATIC_ITEMS',
                                'origin', 'LOCAL',
                                'title', 'Où retirer',
                                'bindsTo', 'withdrawalChannel',
                                'items', jsonb_build_array(
                                        jsonb_build_object(
                                                'id', 'GAB',
                                                'label', 'GAB',
                                                'sub', 'Distributeur automatique',
                                                'icon', 'cash-outline',
                                                'meta', jsonb_build_object('code', 'GAB')
                                        ),
                                        jsonb_build_object(
                                                'id', 'AGENT',
                                                'label', 'Agent',
                                                'sub', 'Code chez un agent',
                                                'icon', 'storefront-outline',
                                                'meta', jsonb_build_object('code', 'AGENT')
                                        ),
                                        jsonb_build_object(
                                                'id', 'AGENCY',
                                                'label', 'Agence',
                                                'sub', 'Guichet agence',
                                                'icon', 'business-outline',
                                                'meta', jsonb_build_object('code', 'AGENCY')
                                        )
                                )
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND (v.configuration -> 'presentation') IS NULL;
