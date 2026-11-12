-- BILL_PAYMENT : brancher l'approbation (criticalFields facture) + bindings evaluate/approve
-- Le BPMN providerBillPay inclut désormais EVALUATE_APPROVAL / APPROVAL_L* / correctBill.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{approvalPolicy,criticalFields}',
        '["amount","currency","billerCode","billReference","productCode","subscriberReference"]'::jsonb,
        true
    )
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND v.status IN ('PUBLISHED', 'DRAFT', 'VALIDATED')
  AND COALESCE((v.configuration -> 'approvalPolicy' ->> 'enabled')::boolean, false) = true;

-- Bindings UI / capability pour les nouvelles étapes (idempotent)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb) || jsonb_build_array(
                jsonb_build_object(
                        'step', 'EVALUATE_APPROVAL',
                        'kind', 'CAPABILITY',
                        'capability', 'EVALUATE_APPROVAL'
                ),
                jsonb_build_object(
                        'step', 'APPROVAL_L1',
                        'kind', 'UI',
                        'formKey', 'account-transfer-approval',
                        'actorRole', 'APPROVER'
                ),
                jsonb_build_object(
                        'step', 'APPROVAL_L2',
                        'kind', 'UI',
                        'formKey', 'account-transfer-approval',
                        'actorRole', 'APPROVER'
                ),
                jsonb_build_object(
                        'step', 'APPROVAL_L3',
                        'kind', 'UI',
                        'formKey', 'account-transfer-approval',
                        'actorRole', 'APPROVER'
                ),
                jsonb_build_object(
                        'step', 'correctBill',
                        'kind', 'UI',
                        'formKey', 'provider-bill-confirm',
                        'actorRole', 'INITIATOR'
                )
        )
    )
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND v.status IN ('PUBLISHED', 'DRAFT', 'VALIDATED')
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'step' = 'EVALUATE_APPROVAL'
    );
