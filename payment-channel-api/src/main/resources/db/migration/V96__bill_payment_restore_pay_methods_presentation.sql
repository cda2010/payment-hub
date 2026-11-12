-- BILL_PAYMENT PUBLISHED : restaurer la présentation (dont payMethods).
-- V90 n'a touché que les versions qui avaient déjà un bloc presentation ;
-- la version PUBLISHED actuelle n'en a plus → le catalogue mobile
-- ne servait pas bindsTo=payProvider (seul le fallback CHECKOUT restait).

UPDATE catalog_de_service_version pub
SET configuration = jsonb_set(
        COALESCE(pub.configuration, '{}'::jsonb),
        '{presentation}',
        COALESCE(
            (
                SELECT arch.configuration -> 'presentation'
                FROM catalog_de_service_version arch
                JOIN catalog_de_service s2 ON s2.id = arch.catalog_de_service_id
                WHERE s2.code = 'BILL_PAYMENT'
                  AND arch.configuration -> 'presentation' -> 'sections' IS NOT NULL
                  AND EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements(
                                arch.configuration -> 'presentation' -> 'sections'
                             ) sec
                        WHERE sec ->> 'bindsTo' = 'payProvider'
                           OR sec ->> 'id' = 'payMethods'
                  )
                ORDER BY arch.updated_at DESC NULLS LAST, arch.created_at DESC NULLS LAST
                LIMIT 1
            ),
            jsonb_build_object(
                'version', 1,
                'channel', 'MOBILE',
                'sections', jsonb_build_array(
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
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE pub.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND pub.status = 'PUBLISHED'
  AND (
        pub.configuration -> 'presentation' IS NULL
     OR NOT EXISTS (
            SELECT 1
            FROM jsonb_array_elements(
                    COALESCE(pub.configuration -> 'presentation' -> 'sections', '[]'::jsonb)
                 ) sec
            WHERE sec ->> 'bindsTo' = 'payProvider'
               OR sec ->> 'id' = 'payMethods'
        )
  );
