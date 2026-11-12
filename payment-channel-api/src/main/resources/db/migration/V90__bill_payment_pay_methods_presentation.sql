-- BILL_PAYMENT : section moyens de paiement (même contrat que CHECKOUT V89).
-- Ajoutée à la présentation existante (favoris / catégories / billers).

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{presentation,sections}',
        COALESCE(v.configuration -> 'presentation' -> 'sections', '[]'::jsonb)
            || jsonb_build_array(
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
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND v.configuration -> 'presentation' IS NOT NULL
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'presentation' -> 'sections', '[]'::jsonb)) sec
        WHERE sec ->> 'bindsTo' = 'payProvider'
           OR sec ->> 'id' = 'payMethods'
    );
