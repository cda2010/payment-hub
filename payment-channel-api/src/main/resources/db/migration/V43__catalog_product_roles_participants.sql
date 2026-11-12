-- P1 casting : roles[] + participants[] sur versions catalogue seedées
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'roles', jsonb_build_array(
                jsonb_build_object(
                        'code', 'SOURCE',
                        'label', 'Compte émetteur',
                        'cardinality', 'ONE_REQUIRED',
                        'selection', 'USER'
                ),
                jsonb_build_object(
                        'code', 'DESTINATION',
                        'label', 'Compte destinataire',
                        'cardinality', 'ONE_REQUIRED',
                        'selection', 'USER'
                )
        ),
        'participants', jsonb_build_array(
                jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_MONEY', 'default', true),
                jsonb_build_object('role', 'SOURCE', 'providerCode', 'WAVE', 'default', false),
                jsonb_build_object('role', 'SOURCE', 'providerCode', 'ORANGE_MONEY', 'default', false),
                jsonb_build_object('role', 'DESTINATION', 'providerCode', 'CORIS_MONEY', 'default', true),
                jsonb_build_object('role', 'DESTINATION', 'providerCode', 'WAVE', 'default', false),
                jsonb_build_object('role', 'DESTINATION', 'providerCode', 'ORANGE_MONEY', 'default', false)
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'roles', jsonb_build_array(
                jsonb_build_object(
                        'code', 'PAYER',
                        'label', 'Payeur (wallet)',
                        'cardinality', 'ONE_REQUIRED',
                        'selection', 'USER'
                ),
                jsonb_build_object(
                        'code', 'BILLER',
                        'label', 'Facturier',
                        'cardinality', 'ONE_REQUIRED',
                        'selection', 'FIXED'
                )
        ),
        'participants', jsonb_build_array(
                jsonb_build_object('role', 'PAYER', 'providerCode', 'CORIS_MONEY', 'default', true),
                jsonb_build_object('role', 'PAYER', 'providerCode', 'WAVE', 'default', false),
                jsonb_build_object('role', 'PAYER', 'providerCode', 'ORANGE_MONEY', 'default', false),
                jsonb_build_object('role', 'BILLER', 'providerCode', 'BILL_PAYMENT', 'default', true)
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT';
