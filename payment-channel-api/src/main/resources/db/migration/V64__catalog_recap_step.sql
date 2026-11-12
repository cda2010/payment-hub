-- Récap générique (formSource=RECAP / product-recap) pour tous les produits transfert / cash-out

-- ACCOUNT_TRANSFER
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb)
    || jsonb_build_object(
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif du transfert',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'fees', 'label', 'Frais'),
                            jsonb_build_object('key', 'sourceRail', 'label', 'Source'),
                            jsonb_build_object('key', 'destRail', 'label', 'Destination')
                    )
            )
       )
    || jsonb_build_object(
            'bindings', (
                SELECT COALESCE(jsonb_agg(
                    CASE
                        WHEN (b->>'step') = 'CONFIRM' THEN
                            b || jsonb_build_object(
                                    'formSource', 'RECAP',
                                    'formKey', 'product-recap'
                            )
                        ELSE b
                    END
                ), '[]'::jsonb)
                FROM jsonb_array_elements(COALESCE(v.configuration->'bindings', '[]'::jsonb)) b
            )
       ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';

-- RTGS
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb)
    || jsonb_build_object(
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif RTGS',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'fees', 'label', 'Frais'),
                            jsonb_build_object('key', 'sourceRail', 'label', 'Banque émettrice'),
                            jsonb_build_object('key', 'destRail', 'label', 'Banque destinataire'),
                            jsonb_build_object('key', 'purpose', 'label', 'Motif')
                    )
            )
       )
    || jsonb_build_object(
            'bindings', (
                SELECT COALESCE(jsonb_agg(
                    CASE
                        WHEN (b->>'step') = 'CONFIRM' THEN
                            b || jsonb_build_object('formSource', 'RECAP', 'formKey', 'product-recap')
                        ELSE b
                    END
                ), '[]'::jsonb)
                FROM jsonb_array_elements(COALESCE(v.configuration->'bindings', '[]'::jsonb)) b
            )
       ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'RTGS';

-- SWIFT
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb)
    || jsonb_build_object(
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif SWIFT',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'fees', 'label', 'Frais'),
                            jsonb_build_object('key', 'bic', 'label', 'BIC'),
                            jsonb_build_object('key', 'iban', 'label', 'IBAN'),
                            jsonb_build_object('key', 'beneficiaryName', 'label', 'Bénéficiaire'),
                            jsonb_build_object('key', 'chargeBearer', 'label', 'Frais à la charge de'),
                            jsonb_build_object('key', 'purpose', 'label', 'Motif')
                    )
            )
       )
    || jsonb_build_object(
            'bindings', (
                SELECT COALESCE(jsonb_agg(
                    CASE
                        WHEN (b->>'step') = 'CONFIRM' THEN
                            b || jsonb_build_object('formSource', 'RECAP', 'formKey', 'product-recap')
                        ELSE b
                    END
                ), '[]'::jsonb)
                FROM jsonb_array_elements(COALESCE(v.configuration->'bindings', '[]'::jsonb)) b
            )
       ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'SWIFT';

-- CASH_OUT
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb)
    || jsonb_build_object(
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif cash-out',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'amount', 'label', 'Montant cash'),
                            jsonb_build_object('key', 'currency', 'label', 'Devise'),
                            jsonb_build_object('key', 'fees', 'label', 'Frais'),
                            jsonb_build_object('key', 'totalDebit', 'label', 'Total débité'),
                            jsonb_build_object('key', 'sourceRail', 'label', 'Compte source'),
                            jsonb_build_object('key', 'phoneNumber', 'label', 'Téléphone'),
                            jsonb_build_object('key', 'agentNetwork', 'label', 'Réseau agent'),
                            jsonb_build_object('key', 'agentCode', 'label', 'Code agent')
                    )
            )
       )
    || jsonb_build_object(
            'bindings', (
                SELECT COALESCE(jsonb_agg(
                    CASE
                        WHEN (b->>'step') = 'CONFIRM' THEN
                            b || jsonb_build_object('formSource', 'RECAP', 'formKey', 'product-recap')
                        ELSE b
                    END
                ), '[]'::jsonb)
                FROM jsonb_array_elements(COALESCE(v.configuration->'bindings', '[]'::jsonb)) b
            )
       ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT';
