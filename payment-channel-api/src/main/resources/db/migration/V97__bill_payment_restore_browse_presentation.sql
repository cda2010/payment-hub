-- BILL_PAYMENT PUBLISHED : si V96 a laissé une présentation réduite à payMethods
-- (fallback), restaurer favoris + catégories + billers + payMethods.
-- payMethods reste pour l'étape gateway ; le mobile filtre bindsTo=payProvider
-- sur la page liste facturiers.

UPDATE catalog_de_service_version pub
SET configuration = jsonb_set(
        COALESCE(pub.configuration, '{}'::jsonb),
        '{presentation}',
        jsonb_build_object(
                'version', 1,
                'channel', 'MOBILE',
                'sections', jsonb_build_array(
                        jsonb_build_object(
                                'id', 'favorites',
                                'displayKind', 'SHORTCUT_SLIDE',
                                'dataSource', 'BILL_FAVORITES',
                                'title', 'Vos favoris',
                                'params', jsonb_build_object('limit', 8),
                                'items', jsonb_build_array(
                                        jsonb_build_object(
                                                'id', 'SONABEL',
                                                'label', 'SONABEL',
                                                'sub', 'Électricité prépayée',
                                                'icon', 'flash-outline',
                                                'meta', jsonb_build_object('code', 'SONABEL')
                                        ),
                                        jsonb_build_object(
                                                'id', 'ONEA',
                                                'label', 'ONEA',
                                                'sub', 'Eau',
                                                'icon', 'water-outline',
                                                'meta', jsonb_build_object('code', 'ONEA')
                                        ),
                                        jsonb_build_object(
                                                'id', 'CANAL+',
                                                'label', 'Canal+',
                                                'sub', 'TV',
                                                'icon', 'tv-outline',
                                                'meta', jsonb_build_object('code', 'CANAL+')
                                        )
                                )
                        ),
                        jsonb_build_object(
                                'id', 'categories',
                                'displayKind', 'FILTER_CHIPS',
                                'dataSource', 'BILLER_CATEGORIES',
                                'title', 'Catégories',
                                'bindsTo', 'billers'
                        ),
                        jsonb_build_object(
                                'id', 'billers',
                                'displayKind', 'LEAF_LIST',
                                'dataSource', 'BILLERS',
                                'searchable', true,
                                'pageable', true,
                                'params', jsonb_build_object('size', 30)
                        ),
                        jsonb_build_object(
                                'id', 'payMethods',
                                'displayKind', 'SHORTCUT_SLIDE',
                                'dataSource', 'CATALOG_PARTICIPANTS',
                                'origin', 'LOCAL',
                                'title', 'Moyen de paiement',
                                'bindsTo', 'payProvider',
                                'params', jsonb_build_object('role', 'PAYER')
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE pub.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND pub.status = 'PUBLISHED'
  AND (
        -- Présentation absente, ou sans section billers (cas V96 fallback payMethods seul)
        pub.configuration -> 'presentation' IS NULL
     OR NOT EXISTS (
            SELECT 1
            FROM jsonb_array_elements(
                    COALESCE(pub.configuration -> 'presentation' -> 'sections', '[]'::jsonb)
                 ) sec
            WHERE sec ->> 'id' = 'billers'
               OR sec ->> 'dataSource' = 'BILLERS'
        )
  );
