-- Presentation catalogue BILL_PAYMENT (displayKind + dataSource pour mobile)

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'presentation', jsonb_build_object(
                'version', 1,
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
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND (v.configuration -> 'presentation') IS NULL;
