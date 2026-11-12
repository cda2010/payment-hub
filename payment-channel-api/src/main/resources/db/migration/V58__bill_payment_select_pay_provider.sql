-- Facture : étape choix wallet après confirmBill (selectPayProvider).
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{bindings}',
        COALESCE(v.configuration->'bindings', '[]'::jsonb) || jsonb_build_array(
                jsonb_build_object(
                        'step', 'selectPayProvider',
                        'kind', 'UI',
                        'actorRole', 'PAYER',
                        'formSource', 'FORM_KEY',
                        'formKey', 'provider-bill-select-pay'
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
        WHERE b->>'step' = 'selectPayProvider'
    );
