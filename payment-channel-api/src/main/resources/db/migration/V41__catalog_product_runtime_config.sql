-- Option B : configuration produit exécutable sur versions catalogue seedées
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'serviceType', 'PAIEMENT_FACTURE',
        'defaultBillProvider', 'BILL_PAYMENT',
        'steps', jsonb_build_array(
                'LOOKUP_BILL',
                'CONFIRM',
                'AUTHENTICATE',
                'DEBIT_WALLET',
                'ACQUIT_BILL',
                'NOTIFY'
        )
     ),
    workflow_key = COALESCE(NULLIF(TRIM(v.workflow_key), ''), 'providerBillPay'),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT';

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'serviceType', 'TRANSFERT',
        'steps', jsonb_build_array(
                'SELECT_SOURCE_ACCOUNT',
                'CAPTURE_DESTINATION',
                'VALIDATE_ACCOUNT',
                'CAPTURE_AMOUNT',
                'CHECK_BALANCE',
                'CHECK_LIMITS',
                'CALCULATE_FEES',
                'SELECT_RAIL',
                'CONFIRM',
                'AUTHENTICATE',
                'DEBIT',
                'EXECUTE_TRANSFER',
                'WAIT_CONFIRMATION',
                'NOTIFY'
        )
     ),
    workflow_key = COALESCE(NULLIF(TRIM(v.workflow_key), ''), 'providerWalletTransfer'),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';
