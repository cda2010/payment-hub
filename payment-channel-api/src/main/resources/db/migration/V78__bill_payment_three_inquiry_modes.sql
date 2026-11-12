-- BILL_PAYMENT : 3 modes (LIST / INFOS / DIRECT) — bindings listBills, selectBill, calculateFees
-- + seed IMPOTS/FONCIER → journey GET_INFOS

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{bindings}',
        COALESCE(v.configuration->'bindings', '[]'::jsonb) || jsonb_build_array(
                jsonb_build_object(
                        'step', 'listBills',
                        'kind', 'INVOKE',
                        'actorRole', 'BILLER',
                        'operation', 'BILL_LIST'
                ),
                jsonb_build_object(
                        'step', 'selectBill',
                        'kind', 'UI',
                        'actorRole', 'BILLER',
                        'formSource', 'FORM_KEY',
                        'formKey', 'provider-bill-select'
                ),
                jsonb_build_object(
                        'step', 'calculateFees',
                        'kind', 'CAPABILITY',
                        'capability', 'CALCULATE_FEES',
                        'operation', 'CALCULATE_FEES'
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
        WHERE b->>'step' = 'listBills'
    );

-- Produits infos (impôts) : mode GET_INFOS
UPDATE biller_product p
SET journey_code = 'GET_INFOS',
    updated_at = NOW()
FROM biller b
WHERE p.biller_id = b.id
  AND upper(b.code) = 'IMPOTS'
  AND upper(p.code) = 'FONCIER'
  AND p.journey_code = 'OPEN_BILLS';
