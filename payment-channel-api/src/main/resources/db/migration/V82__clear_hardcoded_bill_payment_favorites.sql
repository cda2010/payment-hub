-- Retire les favoris hardcodés (SONABEL/ONEA/CANAL+) du seed présentation BILL_PAYMENT.
-- Les items doivent venir de la config admin / LOCAL / OMNICANAL DATASOURCE.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{presentation,sections}',
        (
            SELECT COALESCE(jsonb_agg(
                                    CASE
                                        WHEN sec ->> 'id' = 'favorites'
                                            THEN jsonb_set(sec, '{items}', '[]'::jsonb)
                                        ELSE sec
                                        END
                                    ORDER BY ord
                            ), '[]'::jsonb)
            FROM jsonb_array_elements(COALESCE(v.configuration #> '{presentation,sections}', '[]'::jsonb))
                     WITH ORDINALITY AS t(sec, ord)
        ),
        true
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND v.configuration #> '{presentation,sections}' IS NOT NULL;
