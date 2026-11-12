-- REMITTANCE : envoi d'argent (quote + documents DMN + exécution + payout)
-- Produit shell V38/V39 → workflow remittanceSend + config complète

-- Opérateurs remittance (participant_kind = REMITTANCE)
INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(), v.code, v.libelle, v.description, TRUE, v.ordre,
       'DIRECT', 'OTHER', 'REMITTANCE', NOW(), NOW()
FROM (VALUES
          ('RIA', 'RIA Money Transfer', 'Opérateur remittance RIA', 70),
          ('WESTERN_UNION', 'Western Union', 'Opérateur remittance Western Union', 71)
     ) AS v(code, libelle, description, ordre)
WHERE NOT EXISTS (SELECT 1 FROM payment_provider p WHERE p.code = v.code);

UPDATE payment_provider
SET participant_kind = 'REMITTANCE',
    actif = TRUE,
    updated_at = NOW()
WHERE code IN ('RIA', 'WESTERN_UNION');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code IN ('RIA', 'WESTERN_UNION')
ON CONFLICT DO NOTHING;

-- Hôte technique BPMN catalogue
INSERT INTO payment_provider (
    id, code, libelle, description, actif, ordre_affichage,
    mode_execution, payment_method_type, participant_kind, created_at, updated_at
)
SELECT gen_random_uuid(),
       'REMITTANCE',
       'Remittance (orchestration)',
       'Hôte technique du parcours catalogue REMITTANCE',
       TRUE,
       95,
       'DIRECT',
       'OTHER',
       'RESEAU',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM payment_provider WHERE code = 'REMITTANCE');

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code = 'REMITTANCE'
ON CONFLICT DO NOTHING;

INSERT INTO payment_flow (id, provider_id, code, nom, service_type, bpm_process_key, actif, created_at)
SELECT gen_random_uuid(),
       p.id,
       'REMITTANCE',
       'Envoi remittance',
       'TRANSFERT',
       'remittanceSend',
       TRUE,
       NOW()
FROM payment_provider p
WHERE p.code = 'REMITTANCE'
  AND NOT EXISTS (
      SELECT 1 FROM payment_flow f
      WHERE f.provider_id = p.id AND f.service_type = 'TRANSFERT' AND f.code = 'REMITTANCE'
  );

UPDATE payment_flow f
SET bpm_process_key = 'remittanceSend',
    nom = 'Envoi remittance',
    actif = TRUE
FROM payment_provider p
WHERE f.provider_id = p.id
  AND p.code = 'REMITTANCE'
  AND f.service_type = 'TRANSFERT';

-- Assurer version 1.0
INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       'remittanceSend',
       '{}'::jsonb,
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'REMITTANCE'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

UPDATE catalog_de_service_version v
SET workflow_key = 'remittanceSend',
    status = 'PUBLISHED',
    configuration = jsonb_build_object(
            'serviceType', 'TRANSFERT',
            'terminalOnConfirm', false,
            'minAmount', 1000,
            'defaultFee', 1500,
            'steps', jsonb_build_array(
                    'CAPTURE_SENDER',
                    'SELECT_PROVIDER',
                    'CAPTURE_BENEFICIARY',
                    'SELECT_COUNTRY',
                    'SELECT_PAYOUT_METHOD',
                    'CAPTURE_AMOUNT',
                    'VALIDATE_AMOUNT',
                    'GET_QUOTE',
                    'RESOLVE_DOCUMENTS',
                    'UPLOAD_DOCUMENTS',
                    'VALIDATE_DOCUMENTS',
                    'CONFIRM',
                    'SELECT_FUNDING',
                    'AUTHENTICATE',
                    'EXECUTE_REMITTANCE',
                    'NOTIFY',
                    'WAIT_PAYOUT'
                     ),
            'roles', jsonb_build_array(
                    jsonb_build_object(
                            'code', 'SOURCE',
                            'label', 'Wallet de financement',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    ),
                    jsonb_build_object(
                            'code', 'PROVIDER',
                            'label', 'Opérateur remittance',
                            'cardinality', 'ONE_REQUIRED',
                            'selection', 'USER'
                    )
                     ),
            'participants', jsonb_build_array(
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'WAVE', 'default', true),
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'ORANGE_MONEY', 'default', false),
                    jsonb_build_object('role', 'SOURCE', 'providerCode', 'CORIS_MONEY', 'default', false),
                    jsonb_build_object('role', 'PROVIDER', 'providerCode', 'RIA', 'default', true),
                    jsonb_build_object('role', 'PROVIDER', 'providerCode', 'WESTERN_UNION', 'default', false)
                     ),
            'bindings', jsonb_build_array(
                    jsonb_build_object('step', 'CAPTURE_SENDER', 'kind', 'UI',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-sender'),
                    jsonb_build_object('step', 'SELECT_PROVIDER', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-provider'),
                    jsonb_build_object('step', 'CAPTURE_BENEFICIARY', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-beneficiary'),
                    jsonb_build_object('step', 'SELECT_COUNTRY', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-country'),
                    jsonb_build_object('step', 'SELECT_PAYOUT_METHOD', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-payout'),
                    jsonb_build_object('step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'PROVIDER',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-amount'),
                    jsonb_build_object('step', 'VALIDATE_AMOUNT', 'kind', 'CAPABILITY',
                                       'capability', 'VALIDATE_AMOUNT'),
                    jsonb_build_object('step', 'GET_QUOTE', 'kind', 'CAPABILITY',
                                       'capability', 'GET_QUOTE'),
                    jsonb_build_object('step', 'RESOLVE_DOCUMENTS', 'kind', 'CAPABILITY',
                                       'capability', 'RESOLVE_DOCUMENTS'),
                    jsonb_build_object('step', 'UPLOAD_DOCUMENTS', 'kind', 'UI',
                                       'formSource', 'DOCUMENTS', 'formKey', 'product-document-upload'),
                    jsonb_build_object('step', 'VALIDATE_DOCUMENTS', 'kind', 'CAPABILITY',
                                       'capability', 'VALIDATE_DOCUMENTS'),
                    jsonb_build_object('step', 'CONFIRM', 'kind', 'UI',
                                       'formSource', 'RECAP', 'formKey', 'product-recap'),
                    jsonb_build_object('step', 'SELECT_FUNDING', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'remittance-funding'),
                    jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                       'capability', 'GENERATE_OTP', 'operation', 'GENERATE_OTP'),
                    jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'SOURCE',
                                       'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                    jsonb_build_object('step', 'EXECUTE_REMITTANCE', 'kind', 'INVOKE', 'actorRole', 'PROVIDER',
                                       'capability', 'EXECUTE_REMITTANCE', 'operation', 'REMITTANCE'),
                    jsonb_build_object('step', 'NOTIFY', 'kind', 'CAPABILITY', 'capability', 'NOTIFY'),
                    jsonb_build_object('step', 'WAIT_PAYOUT', 'kind', 'WAIT', 'capability', 'WAIT')
                     ),
            'gateway', jsonb_build_object(
                    'pathAlias', 'remittance',
                    'defaultCurrency', 'XOF',
                    'optionalProviderAtStart', true,
                    'providersQuery', jsonb_build_object(
                            'participantKind', 'REMITTANCE',
                            'serviceType', 'TRANSFERT'
                    ),
                    'hostFlow', jsonb_build_object(
                            'autoCreate', true,
                            'providerCode', 'REMITTANCE',
                            'participantKind', 'RESEAU',
                            'serviceType', 'TRANSFERT'
                    ),
                    'copyRequestFields', jsonb_build_array(
                            'senderName', 'senderPhone', 'beneficiaryName', 'beneficiaryPhone',
                            'destinationCountry', 'receiveCurrency', 'providerCode', 'payoutMethod',
                            'payoutAccount', 'purpose', 'sourceRail', 'amount', 'currency'
                    )
            ),
            'documentPolicy', jsonb_build_object(
                    'enabled', true,
                    'code', 'REMITTANCE_DOCS',
                    'dmnDecisionKey', 'decision_DocumentRequirements',
                    'amountField', 'amount',
                    'formKey', 'product-document-upload'
            ),
            'remittanceQuote', jsonb_build_object(
                    'defaultFee', 1500,
                    'feePercent', 0,
                    'ttlMinutes', 15,
                    'fxRates', jsonb_build_object(
                            'XOF>EUR', 0.00152,
                            'XOF>USD', 0.00165,
                            'XOF>GBP', 0.00130
                    )
            ),
            'recap', jsonb_build_object(
                    'enabled', true,
                    'title', 'Récapitulatif envoi remittance',
                    'confirmField', 'confirmRecap',
                    'fields', jsonb_build_array(
                            jsonb_build_object('key', 'senderName', 'label', 'Expéditeur'),
                            jsonb_build_object('key', 'beneficiaryName', 'label', 'Bénéficiaire'),
                            jsonb_build_object('key', 'destinationCountry', 'label', 'Pays'),
                            jsonb_build_object('key', 'providerCode', 'label', 'Opérateur'),
                            jsonb_build_object('key', 'payoutMethod', 'label', 'Mode réception'),
                            jsonb_build_object('key', 'amount', 'label', 'Montant envoyé'),
                            jsonb_build_object('key', 'receiveAmount', 'label', 'Montant reçu'),
                            jsonb_build_object('key', 'receiveCurrency', 'label', 'Devise reçue'),
                            jsonb_build_object('key', 'fxRate', 'label', 'Taux'),
                            jsonb_build_object('key', 'fees', 'label', 'Frais'),
                            jsonb_build_object('key', 'totalDebit', 'label', 'Total débité'),
                            jsonb_build_object('key', 'quoteId', 'label', 'Réf. cotation')
                    )
            ),
            'softRules', jsonb_build_array(
                    jsonb_build_object(
                            'step', 'VALIDATE_AMOUNT',
                            'type', 'MIN_AMOUNT',
                            'params', jsonb_build_object(
                                    'minAmountKey', 'minAmount',
                                    'errorCode', 'REMITTANCE_MIN_AMOUNT'
                            )
                    ),
                    jsonb_build_object(
                            'step', 'VALIDATE_AMOUNT',
                            'type', 'REQUIRED_FIELD',
                            'params', jsonb_build_object(
                                    'field', 'amount',
                                    'errorCode', 'REMITTANCE_AMOUNT_REQUIRED'
                            )
                    ),
                    jsonb_build_object(
                            'step', 'WAIT_PAYOUT',
                            'type', 'PASS',
                            'params', jsonb_build_object(
                                    'message', 'Envoi disponible pour payout — en attente confirmation agent / provider'
                            )
                    )
            )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE';

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    name = 'Remittance',
    description = 'Envoi d''argent international / régional (quote, documents, payout)',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'REMITTANCE'
  AND v.version = '1.0';

-- Catégorie déjà créée en V40
UPDATE catalog_de_service s
SET categorie_id = c.id,
    updated_at = NOW()
FROM categorie_service c
WHERE s.code = 'REMITTANCE'
  AND c.code = 'REMITTANCE'
  AND (s.categorie_id IS NULL OR s.categorie_id <> c.id);
