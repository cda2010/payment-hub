-- terminalOnConfirm déclaré sur le produit catalogue (plus de Set Java hardcodé).
-- false = le CONFIRM provider n'est pas la fin (NOTIFY / PAY_BILL / post-étapes suivent).

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'terminalOnConfirm', false
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code IN ('ACCOUNT_TRANSFER', 'BILL_PAYMENT')
  AND (v.configuration ->> 'terminalOnConfirm') IS NULL
  AND (v.configuration ->> 'terminal_on_confirm') IS NULL;

-- Exemple produit RTGS : seedé en V60 (workflow_key=rtgsTransfer, terminalOnConfirm=false).
