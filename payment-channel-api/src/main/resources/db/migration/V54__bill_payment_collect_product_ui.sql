-- Facture : collectBill / confirmBill liés au UI schema produit (BILLER_PRODUCT),
-- pas seulement aux formKeys BPMN génériques.
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{bindings}',
        COALESCE(v.configuration->'bindings', '[]'::jsonb) || jsonb_build_array(
                jsonb_build_object(
                        'step', 'collectBill',
                        'kind', 'UI',
                        'actorRole', 'BILLER',
                        'formSource', 'BILLER_PRODUCT',
                        'billerScreen', 'LOOKUP'
                )
        ),
        true
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration->'bindings', '[]'::jsonb)) b
        WHERE b->>'step' = 'collectBill'
    );
