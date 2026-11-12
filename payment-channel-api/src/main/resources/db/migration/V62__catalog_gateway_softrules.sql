-- softRules + gateway (providersQuery / hostFlow) pour produits catalogue existants.
-- Permet d'ajouter un produit sans Java : config JSON + BPMN + forms + publish.

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'gateway', jsonb_build_object(
                'pathAlias', 'account-transfer',
                'defaultCurrency', 'XOF',
                'optionalProviderAtStart', true,
                'providersQuery', jsonb_build_object('source', 'TRANSFER_EMITTERS'),
                'hostFlow', jsonb_build_object('autoCreate', false)
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'gateway', jsonb_build_object(
                'pathAlias', 'rtgs',
                'defaultCurrency', 'XOF',
                'optionalProviderAtStart', true,
                'providersQuery', jsonb_build_object(
                        'participantKind', 'BANQUE',
                        'serviceType', 'TRANSFERT'
                ),
                'hostFlow', jsonb_build_object(
                        'autoCreate', true,
                        'providerCode', 'RTGS',
                        'participantKind', 'RESEAU'
                )
        ),
        'softRules', jsonb_build_array(
                jsonb_build_object(
                        'step', 'CHECK_LIMITS',
                        'type', 'MIN_AMOUNT',
                        'params', jsonb_build_object(
                                'minAmountKey', 'minAmount',
                                'errorCode', 'RTGS_MIN_AMOUNT'
                        )
                ),
                jsonb_build_object(
                        'step', 'CHECK_CUTOFF',
                        'type', 'BUSINESS_HOURS',
                        'params', jsonb_build_object(
                                'zone', 'Africa/Ouagadougou',
                                'from', '08:00',
                                'to', '16:00',
                                'weekdaysOnly', true,
                                'skipFlag', 'skipCutoffCheck',
                                'errorCode', 'RTGS_CUTOFF'
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'RTGS';

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'gateway', jsonb_build_object(
                'pathAlias', 'swift',
                'defaultCurrency', 'EUR',
                'optionalProviderAtStart', true,
                'providersQuery', jsonb_build_object(
                        'participantKind', 'BANQUE',
                        'serviceType', 'TRANSFERT'
                ),
                'hostFlow', jsonb_build_object(
                        'autoCreate', true,
                        'providerCode', 'SWIFT',
                        'participantKind', 'RESEAU'
                )
        ),
        'softRules', jsonb_build_array(
                jsonb_build_object(
                        'step', 'VALIDATE_BIC',
                        'type', 'REGEX_FIELD',
                        'params', jsonb_build_object(
                                'field', 'bic',
                                'pattern', '^[A-Z0-9]{8}([A-Z0-9]{3})?$',
                                'uppercase', true,
                                'errorCode', 'SWIFT_BIC_INVALID',
                                'message', 'BIC/SWIFT invalide (8 ou 11 caractères alphanumériques attendus)'
                        )
                ),
                jsonb_build_object(
                        'step', 'VALIDATE_BIC',
                        'type', 'REQUIRED_FIELD',
                        'params', jsonb_build_object(
                                'field', 'iban',
                                'errorCode', 'SWIFT_IBAN_REQUIRED',
                                'message', 'IBAN bénéficiaire requis'
                        )
                ),
                jsonb_build_object(
                        'step', 'CHECK_COMPLIANCE',
                        'type', 'BLOCKED_VALUES',
                        'params', jsonb_build_object(
                                'field', 'countryCode',
                                'blocked', jsonb_build_array('KP', 'IR', 'SY'),
                                'skipFlag', 'skipComplianceCheck',
                                'errorCode', 'SWIFT_COMPLIANCE_BLOCKED'
                        )
                ),
                jsonb_build_object(
                        'step', 'CHECK_LIMITS',
                        'type', 'MIN_AMOUNT',
                        'params', jsonb_build_object(
                                'minAmountKey', 'minAmount',
                                'errorCode', 'SWIFT_MIN_AMOUNT'
                        )
                ),
                jsonb_build_object(
                        'step', 'CALCULATE_FEES',
                        'type', 'DEFAULT_FEE',
                        'params', jsonb_build_object('amount', 25000)
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'SWIFT';
