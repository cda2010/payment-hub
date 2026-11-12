-- Produit catalogue RTGS (séparé d'ACCOUNT_TRANSFER)
-- Hôte technique : provider RTGS + payment_flow bpm=rtgsTransfer (start sans banque)

INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'RTGS',
       'RTGS (orchestration)',
       'Hôte technique du parcours catalogue RTGS',
       TRUE,
       90,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'RTGS');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'RTGS'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'TRANSFERT',
       'RTGS interbancaire',
       'TRANSFERT',
       'rtgsTransfer',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'RTGS'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT'
  );

INSERT INTO catalog_de_service (id, code, name, description, status, created_at, updated_at, categorie_id)
SELECT gen_random_uuid(),
       'RTGS',
       'Transfert RTGS',
       'Règlement brut temps réel interbancaire',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'TRANSFER'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'RTGS');

UPDATE catalog_de_service
SET name = 'Transfert RTGS',
    description = 'Règlement brut temps réel interbancaire',
    status = 'PUBLISHED',
    updated_at = NOW()
WHERE code = 'RTGS';

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, effective_from, effective_to,
    workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       NOW(),
       NULL,
       'rtgsTransfer',
       jsonb_build_object(
               'serviceType', 'TRANSFERT',
               'terminalOnConfirm', false,
               'minAmount', 1000000,
               'steps', jsonb_build_array(
                       'SELECT_SOURCE_BANK',
                       'CAPTURE_DESTINATION',
                       'CAPTURE_AMOUNT',
                       'VALIDATE_ACCOUNT',
                       'CHECK_LIMITS',
                       'CHECK_CUTOFF',
                       'CALCULATE_FEES',
                       'CONFIRM',
                       'AUTHENTICATE',
                       'EXECUTE_RTGS',
                       'WAIT_SETTLEMENT',
                       'NOTIFY'
                        ),
               'roles', jsonb_build_array(
                       jsonb_build_object(
                               'code', 'SOURCE',
                               'label', 'Banque émettrice',
                               'cardinality', 'ONE_REQUIRED',
                               'selection', 'USER'
                       ),
                       jsonb_build_object(
                               'code', 'DESTINATION',
                               'label', 'Banque destinataire',
                               'cardinality', 'ONE_REQUIRED',
                               'selection', 'USER'
                       )
                        ),
               'participants', jsonb_build_array(
                       jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_BANK', 'default', true),
                       jsonb_build_object('role', 'DESTINATION', 'providerCode', 'CORIS_BANK', 'default', true)
                        ),
               'bindings', jsonb_build_array(
                       jsonb_build_object('step', 'SELECT_SOURCE_BANK', 'kind', 'UI', 'actorRole', 'SOURCE',
                                          'formSource', 'FORM_KEY', 'formKey', 'rtgs-source'),
                       jsonb_build_object('step', 'CAPTURE_DESTINATION', 'kind', 'UI', 'actorRole', 'DESTINATION',
                                          'formSource', 'FORM_KEY', 'formKey', 'rtgs-destination'),
                       jsonb_build_object('step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                                          'formSource', 'FORM_KEY', 'formKey', 'rtgs-amount'),
                       jsonb_build_object('step', 'VALIDATE_ACCOUNT', 'kind', 'SOFT', 'actorRole', 'DESTINATION',
                                          'operation', 'VALIDATE_ACCOUNT'),
                       jsonb_build_object('step', 'CHECK_LIMITS', 'kind', 'SOFT', 'actorRole', 'SOURCE',
                                          'operation', 'LIMITS_CHECK'),
                       jsonb_build_object('step', 'CHECK_CUTOFF', 'kind', 'SOFT', 'operation', 'CHECK_CUTOFF'),
                       jsonb_build_object('step', 'CALCULATE_FEES', 'kind', 'SOFT', 'operation', 'CALCULATE_FEES'),
                       jsonb_build_object('step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'SOURCE',
                                          'formSource', 'FORM_KEY', 'formKey', 'rtgs-confirm'),
                       jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                          'operation', 'GENERATE_OTP'),
                       jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'SOURCE',
                                          'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                       jsonb_build_object('step', 'EXECUTE_RTGS', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                          'operation', 'TRANSFERT'),
                       jsonb_build_object('step', 'WAIT_SETTLEMENT', 'kind', 'WAIT'),
                       jsonb_build_object('step', 'NOTIFY', 'kind', 'SOFT', 'operation', 'NOTIFY')
                        ),
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
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'RTGS'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

UPDATE catalog_de_service s
SET current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'RTGS'
  AND v.version = '1.0'
  AND (s.current_version_id IS NULL OR s.current_version_id <> v.id);

-- Si la version existait déjà (re-run partiel), réaligner workflow + config
UPDATE catalog_de_service_version v
SET workflow_key = 'rtgsTransfer',
    configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'terminalOnConfirm', false,
            'minAmount', 1000000
     ),
    status = 'PUBLISHED',
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'RTGS'
  AND v.version = '1.0';
