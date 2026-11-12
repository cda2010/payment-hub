-- ACCOUNT_TRANSFER : politique d'approbation (DMN TransferApproval)
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'approvalPolicy', jsonb_build_object(
                'enabled', true,
                'decisionKey', 'decision_TransferApproval',
                'selfApprovalAllowed', false,
                'levels', jsonb_build_array(
                        jsonb_build_object(
                                'code', 'L1',
                                'candidateGroup', 'TRANSFER_APPROVER_L1',
                                'formKey', 'account-transfer-approval'
                        ),
                        jsonb_build_object(
                                'code', 'L2',
                                'candidateGroup', 'TRANSFER_APPROVER_L2',
                                'formKey', 'account-transfer-approval'
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';
