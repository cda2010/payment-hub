-- ACCOUNT_TRANSFER : pointer le produit catalogue vers le BPMN 14 steps (accountTransfer)
UPDATE catalog_de_service_version v
SET workflow_key = 'accountTransfer',
    configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
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
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';
